package com.mybarber.agendamento;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mybarber.agenda.ExpedienteDia;
import com.mybarber.agenda.ExpedienteFuncionarioService;
import com.mybarber.agenda.SituacaoExpediente;
import com.mybarber.autenticacao.PerfilAcesso;
import com.mybarber.catalogo.CatalogoServicoService;
import com.mybarber.catalogo.ServicoOferecido;
import com.mybarber.compartilhado.excecao.RegraNegocioException;
import com.mybarber.funcionario.Funcionario;
import com.mybarber.funcionario.FuncionarioService;

@Service
public class HorarioDisponivelService {

    private static final List<StatusAgendamento> STATUS_QUE_OCUPAM_HORARIO =
            List.of(StatusAgendamento.AGENDADO, StatusAgendamento.CONCLUIDO);

    private final AgendamentoRepository agendamentoRepository;
    private final ExpedienteFuncionarioService expedienteFuncionarioService;
    private final FuncionarioService funcionarioService;
    private final CatalogoServicoService catalogoServicoService;
    private final AgendaPropriedades agendaPropriedades;
    private final Clock relogio;

    public HorarioDisponivelService(
            AgendamentoRepository agendamentoRepository,
            ExpedienteFuncionarioService expedienteFuncionarioService,
            FuncionarioService funcionarioService,
            CatalogoServicoService catalogoServicoService,
            AgendaPropriedades agendaPropriedades,
            Clock relogio) {
        this.agendamentoRepository = agendamentoRepository;
        this.expedienteFuncionarioService = expedienteFuncionarioService;
        this.funcionarioService = funcionarioService;
        this.catalogoServicoService = catalogoServicoService;
        this.agendaPropriedades = agendaPropriedades;
        this.relogio = relogio;
    }

    @Transactional(readOnly = true)
    public HorariosDisponiveisResponse listar(
            Long funcionarioId,
            LocalDate data,
            List<Long> servicoIds,
            PerfilAcesso perfilSolicitante) {
        Funcionario funcionario = funcionarioService.buscarBarbeiroAtivoPorId(funcionarioId);
        List<ServicoOferecido> servicos = catalogoServicoService.buscarServicosAtivosParaAtendimento(servicoIds);
        int duracaoTotal = servicos.stream().mapToInt(ServicoOferecido::getDuracaoMinutos).sum();
        BigDecimal valorTabela = servicos.stream().map(ServicoOferecido::getPreco).reduce(BigDecimal.ZERO, BigDecimal::add);

        return new HorariosDisponiveisResponse(
                data, funcionarioId, duracaoTotal, valorTabela,
                calcular(funcionario, data, duracaoTotal, perfilSolicitante));
    }

    /* Cliente vê apenas horários futuros; a equipe também vê horários já passados para registrar atendimentos feitos */
    @Transactional(readOnly = true)
    public List<LocalTime> calcular(
            Funcionario funcionario,
            LocalDate data,
            int duracaoMinutos,
            PerfilAcesso perfilSolicitante) {
        validarData(data, perfilSolicitante);

        ExpedienteDia expediente = expedienteFuncionarioService.obterExpediente(funcionario, data);
        if (expediente.situacao() == SituacaoExpediente.FECHADO) {
            return List.of();
        }

        List<CalculadoraHorariosDisponiveis.PeriodoOcupado> ocupados = agendamentoRepository
                .buscarDoFuncionarioNoPeriodo(
                        funcionario.getId(), data.atStartOfDay(), data.plusDays(1).atStartOfDay(), STATUS_QUE_OCUPAM_HORARIO)
                .stream()
                .map(agendamento -> new CalculadoraHorariosDisponiveis.PeriodoOcupado(
                        agendamento.getInicio(), agendamento.getFim()))
                .toList();

        LocalDateTime inicioMinimo = perfilSolicitante == PerfilAcesso.CLIENTE
                ? LocalDateTime.now(relogio).plus(agendaPropriedades.antecedenciaMinimaAgendamento())
                : data.atStartOfDay();

        return CalculadoraHorariosDisponiveis.calcular(
                data,
                expediente.intervalos(),
                duracaoMinutos,
                agendaPropriedades.intervaloMinutos(),
                ocupados,
                inicioMinimo);
    }

    private void validarData(LocalDate data, PerfilAcesso perfilSolicitante) {
        LocalDate hoje = LocalDate.now(relogio);
        if (perfilSolicitante == PerfilAcesso.ADMINISTRADOR) {
            LocalDate dataMinima = hoje.minusDays(agendaPropriedades.diasMaximosRetroativosAdministrador());
            if (data.isBefore(dataMinima)) {
                throw new RegraNegocioException("Registros retroativos podem ser feitos até "
                        + agendaPropriedades.diasMaximosRetroativosAdministrador() + " dias atrás");
            }
        } else if (perfilSolicitante == PerfilAcesso.BARBEIRO && data.isBefore(hoje)) {
            throw new RegraNegocioException(
                    "Barbeiros podem registrar horários já passados apenas no dia atual. Para outras datas, fale com o administrador");
        } else if (data.isBefore(hoje)) {
            throw new RegraNegocioException("Não é possível agendar em datas passadas");
        }
        if (data.isAfter(hoje.plusDays(agendaPropriedades.diasMaximosAntecedencia()))) {
            throw new RegraNegocioException("Os agendamentos podem ser feitos com até "
                    + agendaPropriedades.diasMaximosAntecedencia() + " dias de antecedência");
        }
    }
}
