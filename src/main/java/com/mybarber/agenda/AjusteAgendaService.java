package com.mybarber.agenda;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mybarber.agendamento.AgendamentoRepository;
import com.mybarber.compartilhado.excecao.ConflitoDadosException;
import com.mybarber.compartilhado.excecao.RecursoNaoEncontradoException;
import com.mybarber.compartilhado.excecao.RegraNegocioException;
import com.mybarber.filial.FilialService;
import com.mybarber.funcionario.Funcionario;
import com.mybarber.funcionario.FuncionarioService;

@Service
public class AjusteAgendaService {

    private final AjusteAgendaRepository ajusteAgendaRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final FuncionarioService funcionarioService;
    private final FilialService filialService;
    private final Clock relogio;

    public AjusteAgendaService(
            AjusteAgendaRepository ajusteAgendaRepository,
            AgendamentoRepository agendamentoRepository,
            FuncionarioService funcionarioService,
            FilialService filialService,
            Clock relogio) {
        this.ajusteAgendaRepository = ajusteAgendaRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.funcionarioService = funcionarioService;
        this.filialService = filialService;
        this.relogio = relogio;
    }

    @Transactional(readOnly = true)
    public List<AjusteAgenda> listar(LocalDate inicio, LocalDate fim, Long filialId) {
        Long idFilial = filialService.buscarAtivaOuPadrao(filialId).getId();
        return ajusteAgendaRepository.findAllByFilialIdAndDataBetweenOrderByDataAsc(idFilial, inicio, fim);
    }

    /* Se já existir um ajuste para o mesmo dia e escopo, ele é substituído */
    @Transactional
    public AjusteAgenda salvar(AjusteAgendaRequest requisicao, Long idAdministrador) {
        validar(requisicao);

        AjusteAgenda ajuste;
        if (requisicao.funcionarioId() != null) {
            Funcionario funcionario = funcionarioService.buscarBarbeiroAtivoPorId(requisicao.funcionarioId());
            ajuste = ajusteAgendaRepository.findByFuncionarioIdAndData(funcionario.getId(), requisicao.data())
                    .orElseGet(AjusteAgenda::new);
            ajuste.setFuncionarioId(funcionario.getId());
            ajuste.setFilialId(funcionario.getFilial().getId());
        } else {
            Long idFilial = filialService.buscarAtivaOuPadrao(requisicao.filialId()).getId();
            ajuste = ajusteAgendaRepository.findByFilialIdAndDataAndFuncionarioIdIsNull(idFilial, requisicao.data())
                    .orElseGet(AjusteAgenda::new);
            ajuste.setFilialId(idFilial);
        }

        validarAgendamentosAfetados(ajuste.getFilialId(), ajuste.getFuncionarioId(), requisicao);

        ajuste.setData(requisicao.data());
        ajuste.setTipo(requisicao.tipo());
        ajuste.setHoraInicio(requisicao.tipo() == TipoAjusteAgenda.ABERTO ? requisicao.horaInicio() : null);
        ajuste.setHoraFim(requisicao.tipo() == TipoAjusteAgenda.ABERTO ? requisicao.horaFim() : null);
        ajuste.setMotivo(requisicao.motivo() == null || requisicao.motivo().isBlank() ? null : requisicao.motivo().strip());
        ajuste.setCriadoPorFuncionarioId(idAdministrador);

        return ajusteAgendaRepository.saveAndFlush(ajuste);
    }

    @Transactional
    public void remover(Long id) {
        AjusteAgenda ajuste = ajusteAgendaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ajuste de agenda não encontrado com o id: " + id));
        if (ajuste.getData().isBefore(LocalDate.now(relogio))) {
            throw new RegraNegocioException("Não é possível remover ajustes de datas passadas");
        }
        ajusteAgendaRepository.delete(ajuste);
    }

    private void validarAgendamentosAfetados(Long filialId, Long funcionarioId, AjusteAgendaRequest requisicao) {
        LocalDate data = requisicao.data();
        Set<Long> funcionariosComAjusteProprio = funcionarioId != null
                ? Set.of()
                : ajusteAgendaRepository.findAllByFilialIdAndDataBetweenOrderByDataAsc(filialId, data, data).stream()
                        .filter(ajuste -> !ajuste.isGeral())
                        .map(AjusteAgenda::getFuncionarioId)
                        .collect(Collectors.toSet());

        long quantidadeAfetados = agendamentoRepository
                .buscarAgendadosDaFilialNoPeriodo(filialId, data.atStartOfDay(), data.plusDays(1).atStartOfDay())
                .stream()
                .filter(agendamento -> funcionarioId == null
                        ? !funcionariosComAjusteProprio.contains(agendamento.getFuncionario().getId())
                        : funcionarioId.equals(agendamento.getFuncionario().getId()))
                .filter(agendamento -> requisicao.tipo() == TipoAjusteAgenda.FECHADO
                        || agendamento.getInicio().toLocalTime().isBefore(requisicao.horaInicio())
                        || agendamento.getFim().toLocalTime().isAfter(requisicao.horaFim()))
                .count();

        if (quantidadeAfetados > 0) {
            throw new ConflitoDadosException("Existem " + quantidadeAfetados
                    + " agendamento(s) ativo(s) fora do novo horário nesse dia. Cancele ou reagende antes de ajustar a agenda");
        }
    }

    private void validar(AjusteAgendaRequest requisicao) {
        if (requisicao.data().isBefore(LocalDate.now(relogio))) {
            throw new RegraNegocioException("Não é possível ajustar a agenda de datas passadas");
        }
        if (requisicao.tipo() == TipoAjusteAgenda.ABERTO) {
            if (requisicao.horaInicio() == null || requisicao.horaFim() == null) {
                throw new RegraNegocioException("Informe a hora de início e de fim para abrir a agenda");
            }
            if (!requisicao.horaFim().isAfter(requisicao.horaInicio())) {
                throw new RegraNegocioException("A hora de fim deve ser posterior à hora de início");
            }
        }
    }
}
