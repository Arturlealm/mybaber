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
    private final AjusteAgendaRepository ajusteAgendaRepository;
    private final FuncionarioService funcionarioService;
    private final FilialService filialService;

    public ExpedienteFuncionarioService(
            JornadaFuncionarioRepository jornadaFuncionarioRepository,
            AjusteAgendaRepository ajusteAgendaRepository,
            FuncionarioService funcionarioService,
            FilialService filialService) {
        this.jornadaFuncionarioRepository = jornadaFuncionarioRepository;
        this.ajusteAgendaRepository = ajusteAgendaRepository;
        this.funcionarioService = funcionarioService;
        this.filialService = filialService;
    }

    @Transactional(readOnly = true)
    public ExpedienteDia obterExpediente(Funcionario funcionario, LocalDate data) {
        DiaSemana diaSemana = DiaSemana.de(data);
        List<JornadaFuncionario> jornadasDoDia = jornadaFuncionarioRepository.findAllByFuncionarioId(funcionario.getId())
                .stream()
                .filter(jornada -> jornada.getDiaSemana() == diaSemana)
                .toList();

        AjusteAgenda ajusteFuncionario = ajusteAgendaRepository
                .findByFuncionarioIdAndData(funcionario.getId(), data)
                .orElse(null);
        AjusteAgenda ajusteGeral = ajusteAgendaRepository
                .findByFilialIdAndDataAndFuncionarioIdIsNull(funcionario.getFilial().getId(), data)
                .orElse(null);

        return ExpedienteDia.calcular(jornadasDoDia, ajusteFuncionario, ajusteGeral);
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

        Map<Long, List<JornadaFuncionario>> jornadasPorFuncionario = jornadaFuncionarioRepository
                .findAllByFuncionarioIdIn(funcionarios.stream().map(Funcionario::getId).toList())
                .stream()
                .collect(Collectors.groupingBy(JornadaFuncionario::getFuncionarioId));
        List<AjusteAgenda> ajustes = ajusteAgendaRepository
                .findAllByFilialIdAndDataBetweenOrderByDataAsc(filial.getId(), inicio, fim);

        List<CalendarioAgendaDiaResponse> dias = new ArrayList<>();
        for (LocalDate data = inicio; !data.isAfter(fim); data = data.plusDays(1)) {
            dias.add(montarDia(data, funcionarios, jornadasPorFuncionario, ajustes));
        }
        return dias;
    }

    private CalendarioAgendaDiaResponse montarDia(
            LocalDate data,
            List<Funcionario> funcionarios,
            Map<Long, List<JornadaFuncionario>> jornadasPorFuncionario,
            List<AjusteAgenda> ajustes) {
        DiaSemana diaSemana = DiaSemana.de(data);
        AjusteAgenda ajusteGeral = ajustes.stream()
                .filter(ajuste -> ajuste.isGeral() && ajuste.getData().equals(data))
                .findFirst()
                .orElse(null);

        List<CalendarioAgendaDiaResponse.Funcionario> situacaoFuncionarios = funcionarios.stream()
                .map(funcionario -> {
                    List<JornadaFuncionario> jornadasDoDia = jornadasPorFuncionario
                            .getOrDefault(funcionario.getId(), List.of())
                            .stream()
                            .filter(jornada -> jornada.getDiaSemana() == diaSemana)
                            .toList();
                    AjusteAgenda ajusteFuncionario = ajustes.stream()
                            .filter(ajuste -> funcionario.getId().equals(ajuste.getFuncionarioId())
                                    && ajuste.getData().equals(data))
                            .findFirst()
                            .orElse(null);
                    ExpedienteDia expediente = ExpedienteDia.calcular(jornadasDoDia, ajusteFuncionario, ajusteGeral);
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
                diaSemana,
                ajusteGeral == null ? null : AjusteAgendaResponse.de(ajusteGeral),
                situacaoFuncionarios);
    }
}
