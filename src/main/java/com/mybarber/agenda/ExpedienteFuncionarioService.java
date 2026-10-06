package com.mybarber.agenda;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mybarber.compartilhado.excecao.RegraNegocioException;
import com.mybarber.filial.Filial;
import com.mybarber.filial.FilialService;
import com.mybarber.funcionario.Funcionario;
import com.mybarber.funcionario.FuncionarioService;

@Service
public class ExpedienteFuncionarioService {

    private static final int QUANTIDADE_MAXIMA_DIAS_CALENDARIO = 62;

    private final JornadaFuncionarioRepository jornadaFuncionarioRepository;
    private final JornadaFilialRepository jornadaFilialRepository;
    private final AjusteAgendaRepository ajusteAgendaRepository;
    private final FuncionarioService funcionarioService;
    private final FilialService filialService;

    public ExpedienteFuncionarioService(
            JornadaFuncionarioRepository jornadaFuncionarioRepository,
            JornadaFilialRepository jornadaFilialRepository,
            AjusteAgendaRepository ajusteAgendaRepository,
            FuncionarioService funcionarioService,
            FilialService filialService) {
        this.jornadaFuncionarioRepository = jornadaFuncionarioRepository;
        this.jornadaFilialRepository = jornadaFilialRepository;
        this.ajusteAgendaRepository = ajusteAgendaRepository;
        this.funcionarioService = funcionarioService;
        this.filialService = filialService;
    }

    @Transactional(readOnly = true)
    public ExpedienteDia obterExpediente(Funcionario funcionario, LocalDate data) {
        Long filialId = funcionario.getFilial().getId();
        JornadaSemanal jornada = JornadaSemanal.resolver(
                jornadaFuncionarioRepository.findAllByFuncionarioId(funcionario.getId()),
                jornadaFilialRepository.findAllByFilialId(filialId));

        AjusteAgenda ajusteFuncionario = ajusteAgendaRepository
                .findByFuncionarioIdAndData(funcionario.getId(), data)
                .orElse(null);
        AjusteAgenda ajusteGeral = ajusteAgendaRepository
                .findByFilialIdAndDataAndFuncionarioIdIsNull(filialId, data)
                .orElse(null);

        return ExpedienteDia.calcular(jornada.intervalosDe(data), jornada.origem(), ajusteFuncionario, ajusteGeral);
    }

    @Transactional(readOnly = true)
    public List<CalendarioAgendaDiaResponse> montarCalendario(
            LocalDate inicio,
            LocalDate fim,
            Long filialId,
            Long funcionarioId) {
        if (fim.isBefore(inicio)) {
            throw new RegraNegocioException("A data final deve ser igual ou posterior à data inicial");
        }
        if (ChronoUnit.DAYS.between(inicio, fim) >= QUANTIDADE_MAXIMA_DIAS_CALENDARIO) {
            throw new RegraNegocioException(
                    "O período do calendário deve ter no máximo " + QUANTIDADE_MAXIMA_DIAS_CALENDARIO + " dias");
        }

        List<Funcionario> funcionarios;
        Filial filial;
        if (funcionarioId != null) {
            Funcionario funcionario = funcionarioService.buscarBarbeiroAtivoPorId(funcionarioId);
            funcionarios = List.of(funcionario);
            filial = funcionario.getFilial();
        } else {
            filial = filialService.buscarAtivaOuPadrao(filialId);
            funcionarios = funcionarioService.listarBarbeirosDisponiveis(filial.getId());
        }

        List<JornadaFilial> jornadaDaFilial = jornadaFilialRepository.findAllByFilialId(filial.getId());
        Map<Long, List<JornadaFuncionario>> jornadasPorFuncionario = jornadaFuncionarioRepository
                .findAllByFuncionarioIdIn(funcionarios.stream().map(Funcionario::getId).toList())
                .stream()
                .collect(Collectors.groupingBy(JornadaFuncionario::getFuncionarioId));
        Map<Long, JornadaSemanal> jornadaPorFuncionario = funcionarios.stream()
                .collect(Collectors.toMap(
                        Funcionario::getId,
                        funcionario -> JornadaSemanal.resolver(
                                jornadasPorFuncionario.getOrDefault(funcionario.getId(), List.of()), jornadaDaFilial)));
        List<AjusteAgenda> ajustes = ajusteAgendaRepository
                .findAllByFilialIdAndDataBetweenOrderByDataAsc(filial.getId(), inicio, fim);

        List<CalendarioAgendaDiaResponse> dias = new ArrayList<>();
        for (LocalDate data = inicio; !data.isAfter(fim); data = data.plusDays(1)) {
            dias.add(montarDia(data, funcionarios, jornadaPorFuncionario, ajustes));
        }
        return dias;
    }

    private CalendarioAgendaDiaResponse montarDia(
            LocalDate data,
            List<Funcionario> funcionarios,
            Map<Long, JornadaSemanal> jornadaPorFuncionario,
            List<AjusteAgenda> ajustes) {
        AjusteAgenda ajusteGeral = ajustes.stream()
                .filter(ajuste -> ajuste.isGeral() && ajuste.getData().equals(data))
                .findFirst()
                .orElse(null);

        List<CalendarioAgendaDiaResponse.Funcionario> situacaoFuncionarios = funcionarios.stream()
                .map(funcionario -> {
                    JornadaSemanal jornada = jornadaPorFuncionario.get(funcionario.getId());
                    AjusteAgenda ajusteFuncionario = ajustes.stream()
                            .filter(ajuste -> funcionario.getId().equals(ajuste.getFuncionarioId())
                                    && ajuste.getData().equals(data))
                            .findFirst()
                            .orElse(null);
                    ExpedienteDia expediente = ExpedienteDia.calcular(
                            jornada.intervalosDe(data), jornada.origem(), ajusteFuncionario, ajusteGeral);
                    return new CalendarioAgendaDiaResponse.Funcionario(
                            funcionario.getId(),
                            funcionario.getNome(),
                            expediente.situacao(),
                            expediente.origem(),
                            expediente.ajusteId(),
                            expediente.motivo(),
                            expediente.intervalos());
                })
                .toList();

        return new CalendarioAgendaDiaResponse(
                data,
                DiaSemana.de(data),
                ajusteGeral == null ? null : AjusteAgendaResponse.de(ajusteGeral),
                situacaoFuncionarios);
    }

    /* O barbeiro com jornada própria segue a sua; sem jornada própria, segue a jornada padrão da filial */
    private record JornadaSemanal(OrigemExpediente origem, Map<DiaSemana, List<IntervaloHorario>> intervalosPorDia) {

        static JornadaSemanal resolver(List<JornadaFuncionario> doFuncionario, List<JornadaFilial> daFilial) {
            if (!doFuncionario.isEmpty()) {
                return new JornadaSemanal(OrigemExpediente.JORNADA_FUNCIONARIO, doFuncionario.stream()
                        .collect(Collectors.groupingBy(
                                JornadaFuncionario::getDiaSemana,
                                Collectors.mapping(JornadaFuncionario::paraIntervalo, Collectors.toList()))));
            }
            return new JornadaSemanal(OrigemExpediente.JORNADA_FILIAL, daFilial.stream()
                    .collect(Collectors.groupingBy(
                            JornadaFilial::getDiaSemana,
                            Collectors.mapping(JornadaFilial::paraIntervalo, Collectors.toList()))));
        }

        List<IntervaloHorario> intervalosDe(LocalDate data) {
            return intervalosPorDia.getOrDefault(DiaSemana.de(data), List.of());
        }
    }
}
