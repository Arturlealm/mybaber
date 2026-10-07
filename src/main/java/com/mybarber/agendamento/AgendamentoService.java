package com.mybarber.agendamento;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mybarber.autenticacao.UsuarioAutenticado;
import com.mybarber.catalogo.CatalogoServicoService;
import com.mybarber.catalogo.ServicoOferecido;
import com.mybarber.cliente.Cliente;
import com.mybarber.cliente.ClienteService;
import com.mybarber.compartilhado.excecao.ConflitoDadosException;
import com.mybarber.compartilhado.excecao.RecursoNaoEncontradoException;
import com.mybarber.compartilhado.excecao.RegraNegocioException;
import com.mybarber.filial.FilialService;
import com.mybarber.funcionario.Funcionario;
import com.mybarber.funcionario.FuncionarioService;

@Service
public class AgendamentoService {

    private static final int QUANTIDADE_MAXIMA_DIAS_AGENDA = 62;

    private final AgendamentoRepository agendamentoRepository;
    private final HorarioDisponivelService horarioDisponivelService;
    private final ClienteService clienteService;
    private final FuncionarioService funcionarioService;
    private final FilialService filialService;
    private final CatalogoServicoService catalogoServicoService;
    private final AgendaPropriedades agendaPropriedades;
    private final Clock relogio;

    public AgendamentoService(
            AgendamentoRepository agendamentoRepository,
            HorarioDisponivelService horarioDisponivelService,
            ClienteService clienteService,
            FuncionarioService funcionarioService,
            FilialService filialService,
            CatalogoServicoService catalogoServicoService,
            AgendaPropriedades agendaPropriedades,
            Clock relogio) {
        this.agendamentoRepository = agendamentoRepository;
        this.horarioDisponivelService = horarioDisponivelService;
        this.clienteService = clienteService;
        this.funcionarioService = funcionarioService;
        this.filialService = filialService;
        this.catalogoServicoService = catalogoServicoService;
        this.agendaPropriedades = agendaPropriedades;
        this.relogio = relogio;
    }

    @Transactional
    public Agendamento criar(AgendamentoCriacaoRequest requisicao, UsuarioAutenticado usuario) {
        Cliente cliente = definirCliente(requisicao, usuario);
        Funcionario funcionario = funcionarioService.buscarBarbeiroAtivoPorId(requisicao.funcionarioId());
        List<ServicoOferecido> servicos = catalogoServicoService.buscarServicosAtivosParaAtendimento(requisicao.servicoIds());

        if (usuario.isBarbeiro() && !funcionario.getId().equals(usuario.id())) {
            throw new RegraNegocioException("Barbeiros podem registrar agendamentos apenas na própria agenda");
        }

        Agendamento agendamento = new Agendamento(cliente, funcionario, requisicao.inicio(), servicos);
        int duracaoTotal = agendamento.getItens().stream().mapToInt(AgendamentoItem::getDuracaoMinutos).sum();

        boolean horarioDisponivel = horarioDisponivelService
                .calcular(funcionario, requisicao.inicio().toLocalDate(), duracaoTotal, usuario.perfil())
                .contains(requisicao.inicio().toLocalTime());
        if (!horarioDisponivel) {
            throw new RegraNegocioException("O horário escolhido não está disponível para este barbeiro");
        }
        if (agendamentoRepository.existeAgendamentoDoClienteNoPeriodo(
                cliente.getId(), agendamento.getInicio(), agendamento.getFim())) {
            throw new RegraNegocioException("O cliente já possui um agendamento neste horário");
        }
        if (requisicao.registrarComoRealizado()) {
            registrarAtendimentoRealizado(agendamento, requisicao, usuario);
        }

        try {
            return agendamentoRepository.saveAndFlush(agendamento);
        } catch (DataIntegrityViolationException excecao) {
            throw new ConflitoDadosException("Este horário acabou de ser reservado. Escolha outro horário");
        }
    }

    @Transactional(readOnly = true)
    public Agendamento buscar(Long id, UsuarioAutenticado usuario) {
        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Agendamento não encontrado com o id: " + id));

        boolean permitido = usuario.isAdministrador()
                || (usuario.isCliente() && agendamento.getCliente().getId().equals(usuario.id()))
                || (usuario.isBarbeiro() && agendamento.getFuncionario().getId().equals(usuario.id()));
        if (!permitido) {
            throw new RecursoNaoEncontradoException("Agendamento não encontrado com o id: " + id);
        }
        return agendamento;
    }

    @Transactional(readOnly = true)
    public Page<Agendamento> listarDoCliente(Long clienteId, Pageable paginacao) {
        return agendamentoRepository.findAllByClienteIdOrderByInicioDesc(clienteId, paginacao);
    }

    @Transactional(readOnly = true)
    public List<Agendamento> listarAgendaDoDia(
            LocalDate data,
            Long filialId,
            Long funcionarioId,
            UsuarioAutenticado usuario) {
        return listarAgendaDoPeriodo(data, data, filialId, funcionarioId, usuario);
    }

    /* O barbeiro sempre vê apenas a própria agenda; o administrador pode filtrar por barbeiro ou ver a filial inteira */
    @Transactional(readOnly = true)
    public List<Agendamento> listarAgendaDoPeriodo(
            LocalDate inicio,
            LocalDate fim,
            Long filialId,
            Long funcionarioId,
            UsuarioAutenticado usuario) {
        if (fim.isBefore(inicio)) {
            throw new RegraNegocioException("A data final deve ser igual ou posterior à data inicial");
        }
        if (ChronoUnit.DAYS.between(inicio, fim) >= QUANTIDADE_MAXIMA_DIAS_AGENDA) {
            throw new RegraNegocioException(
                    "O período da agenda deve ter no máximo " + QUANTIDADE_MAXIMA_DIAS_AGENDA + " dias");
        }

        LocalDateTime inicioPeriodo = inicio.atStartOfDay();
        LocalDateTime fimPeriodo = fim.plusDays(1).atStartOfDay();

        Long idFuncionario = usuario.isBarbeiro() ? usuario.id() : funcionarioId;
        if (idFuncionario != null) {
            return agendamentoRepository.buscarDoFuncionarioNoPeriodo(
                    idFuncionario, inicioPeriodo, fimPeriodo, List.of(StatusAgendamento.values()));
        }

        Long idFilial = filialService.buscarAtivaOuPadrao(filialId).getId();
        return agendamentoRepository.buscarDaFilialNoPeriodo(idFilial, inicioPeriodo, fimPeriodo);
    }

    @Transactional
    public Agendamento cancelar(Long id, AgendamentoCancelamentoRequest requisicao, UsuarioAutenticado usuario) {
        Agendamento agendamento = buscar(id, usuario);
        exigirStatusAgendado(agendamento);

        if (usuario.isCliente()) {
            LocalDateTime limiteCancelamento = agendamento.getInicio()
                    .minus(agendaPropriedades.antecedenciaMinimaCancelamentoCliente());
            if (LocalDateTime.now(relogio).isAfter(limiteCancelamento)) {
                throw new RegraNegocioException("O cancelamento pelo sistema deve ser feito com pelo menos "
                        + agendaPropriedades.antecedenciaMinimaCancelamentoCliente().toHours()
                        + " horas de antecedência. Entre em contato com a barbearia");
            }
        }

        OrigemCancelamento origem = usuario.isCliente() ? OrigemCancelamento.CLIENTE : OrigemCancelamento.FUNCIONARIO;
        agendamento.cancelar(origem, textoOuNulo(requisicao == null ? null : requisicao.motivo()), Instant.now(relogio));
        return agendamento;
    }

    @Transactional
    public Agendamento concluir(Long id, AgendamentoConclusaoRequest requisicao, UsuarioAutenticado usuario) {
        Agendamento agendamento = buscar(id, usuario);
        exigirStatusAgendado(agendamento);
        exigirHorarioIniciado(agendamento);

        BigDecimal valorCobrado = requisicao.valorCobrado() == null
                ? agendamento.getValorTabela()
                : requisicao.valorCobrado();
        agendamento.concluir(valorCobrado, textoOuNulo(requisicao.observacao()), usuario.id(), Instant.now(relogio));
        return agendamento;
    }

    @Transactional
    public Agendamento registrarNaoComparecimento(Long id, UsuarioAutenticado usuario) {
        Agendamento agendamento = buscar(id, usuario);
        exigirStatusAgendado(agendamento);
        exigirHorarioIniciado(agendamento);

        agendamento.registrarNaoComparecimento(usuario.id(), Instant.now(relogio));
        return agendamento;
    }

    private void registrarAtendimentoRealizado(
            Agendamento agendamento,
            AgendamentoCriacaoRequest requisicao,
            UsuarioAutenticado usuario) {
        if (usuario.isCliente()) {
            throw new RegraNegocioException("Apenas a equipe pode registrar atendimentos já realizados");
        }
        exigirHorarioIniciado(agendamento);

        BigDecimal valorCobrado = requisicao.valorCobrado() == null
                ? agendamento.getValorTabela()
                : requisicao.valorCobrado();
        agendamento.concluir(valorCobrado, textoOuNulo(requisicao.observacao()), usuario.id(), Instant.now(relogio));
    }

    private Cliente definirCliente(AgendamentoCriacaoRequest requisicao, UsuarioAutenticado usuario) {
        if (usuario.isCliente()) {
            return clienteService.buscarAtivoPorId(usuario.id());
        }
        if (requisicao.clienteId() == null) {
            throw new RegraNegocioException("Informe o cliente do agendamento");
        }
        return clienteService.buscarAtivoPorId(requisicao.clienteId());
    }

    private void exigirStatusAgendado(Agendamento agendamento) {
        if (agendamento.getStatus() != StatusAgendamento.AGENDADO) {
            throw new RegraNegocioException("Somente agendamentos com status AGENDADO podem ser alterados");
        }
    }

    private void exigirHorarioIniciado(Agendamento agendamento) {
        if (LocalDateTime.now(relogio).isBefore(agendamento.getInicio())) {
            throw new RegraNegocioException("Esta ação só é permitida a partir do horário do agendamento");
        }
    }

    private String textoOuNulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.strip();
    }
}
