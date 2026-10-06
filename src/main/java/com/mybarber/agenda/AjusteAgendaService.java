package com.mybarber.agenda;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mybarber.compartilhado.excecao.RecursoNaoEncontradoException;
import com.mybarber.compartilhado.excecao.RegraNegocioException;
import com.mybarber.filial.FilialService;
import com.mybarber.funcionario.Funcionario;
import com.mybarber.funcionario.FuncionarioService;

@Service
public class AjusteAgendaService {

    private final AjusteAgendaRepository ajusteAgendaRepository;
    private final FuncionarioService funcionarioService;
    private final FilialService filialService;
    private final Clock relogio;

    public AjusteAgendaService(
            AjusteAgendaRepository ajusteAgendaRepository,
            FuncionarioService funcionarioService,
            FilialService filialService,
            Clock relogio) {
        this.ajusteAgendaRepository = ajusteAgendaRepository;
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
