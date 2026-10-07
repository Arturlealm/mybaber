package com.mybarber.agendamento;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.mybarber.catalogo.ServicoOferecido;
import com.mybarber.cliente.Cliente;
import com.mybarber.funcionario.Funcionario;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "agendamentos")
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(optional = false)
    @JoinColumn(name = "funcionario_id", nullable = false)
    private Funcionario funcionario;

    @Column(name = "filial_id", nullable = false)
    private Long filialId;

    @Column(nullable = false)
    private LocalDateTime inicio;

    @Column(nullable = false)
    private LocalDateTime fim;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusAgendamento status;

    @Column(name = "valor_tabela", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTabela;

    @Column(name = "valor_cobrado", precision = 10, scale = 2)
    private BigDecimal valorCobrado;

    @Column(length = 255)
    private String observacao;

    @Column(name = "concluido_em")
    private Instant concluidoEm;

    @Column(name = "concluido_por_funcionario_id")
    private Long concluidoPorFuncionarioId;

    @Column(name = "cancelado_em")
    private Instant canceladoEm;

    @Enumerated(EnumType.STRING)
    @Column(name = "cancelado_por", length = 20)
    private OrigemCancelamento canceladoPor;

    @Column(name = "motivo_cancelamento", length = 255)
    private String motivoCancelamento;

    @Column(name = "lembrete_enviado_em", insertable = false, updatable = false)
    private Instant lembreteEnviadoEm;

    @OneToMany(mappedBy = "agendamento", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<AgendamentoItem> itens = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    protected Agendamento() {
    }

    public Agendamento(Cliente cliente, Funcionario funcionario, LocalDateTime inicio, List<ServicoOferecido> servicos) {
        this.cliente = cliente;
        this.funcionario = funcionario;
        this.filialId = funcionario.getFilial().getId();
        this.inicio = inicio;
        this.status = StatusAgendamento.AGENDADO;
        servicos.forEach(servico -> itens.add(new AgendamentoItem(this, servico)));
        this.fim = inicio.plusMinutes(itens.stream().mapToInt(AgendamentoItem::getDuracaoMinutos).sum());
        this.valorTabela = itens.stream().map(AgendamentoItem::getPrecoTabela).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void concluir(BigDecimal valorCobrado, String observacao, Long idFuncionario, Instant agora) {
        this.status = StatusAgendamento.CONCLUIDO;
        this.valorCobrado = valorCobrado;
        this.observacao = observacao;
        this.concluidoPorFuncionarioId = idFuncionario;
        this.concluidoEm = agora;
    }

    public void cancelar(OrigemCancelamento origem, String motivo, Instant agora) {
        this.status = StatusAgendamento.CANCELADO;
        this.canceladoPor = origem;
        this.motivoCancelamento = motivo;
        this.canceladoEm = agora;
    }

    public void registrarNaoComparecimento(Long idFuncionario, Instant agora) {
        this.status = StatusAgendamento.NAO_COMPARECEU;
        this.concluidoPorFuncionarioId = idFuncionario;
        this.concluidoEm = agora;
    }

    public Long getId() {
        return id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Funcionario getFuncionario() {
        return funcionario;
    }

    public Long getFilialId() {
        return filialId;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public LocalDateTime getFim() {
        return fim;
    }

    public StatusAgendamento getStatus() {
        return status;
    }

    public BigDecimal getValorTabela() {
        return valorTabela;
    }

    public BigDecimal getValorCobrado() {
        return valorCobrado;
    }

    public String getObservacao() {
        return observacao;
    }

    public Instant getConcluidoEm() {
        return concluidoEm;
    }

    public OrigemCancelamento getCanceladoPor() {
        return canceladoPor;
    }

    public String getMotivoCancelamento() {
        return motivoCancelamento;
    }

    public List<AgendamentoItem> getItens() {
        return itens;
    }

    public Instant getLembreteEnviadoEm() {
        return lembreteEnviadoEm;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
}
