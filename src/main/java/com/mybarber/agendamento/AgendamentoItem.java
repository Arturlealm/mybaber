package com.mybarber.agendamento;

import java.math.BigDecimal;

import com.mybarber.catalogo.ServicoOferecido;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "agendamentos_itens")
public class AgendamentoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agendamento_id", nullable = false)
    private Agendamento agendamento;

    @Column(name = "servico_id", nullable = false)
    private Long servicoId;

    @Column(name = "nome_servico", nullable = false, length = 100)
    private String nomeServico;

    @Column(name = "duracao_minutos", nullable = false)
    private int duracaoMinutos;

    @Column(name = "preco_tabela", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoTabela;

    protected AgendamentoItem() {
    }

    AgendamentoItem(Agendamento agendamento, ServicoOferecido servico) {
        this.agendamento = agendamento;
        this.servicoId = servico.getId();
        this.nomeServico = servico.getNome();
        this.duracaoMinutos = servico.getDuracaoMinutos();
        this.precoTabela = servico.getPreco();
    }

    public Long getId() {
        return id;
    }

    public Long getServicoId() {
        return servicoId;
    }

    public String getNomeServico() {
        return nomeServico;
    }

    public int getDuracaoMinutos() {
        return duracaoMinutos;
    }

    public BigDecimal getPrecoTabela() {
        return precoTabela;
    }
}
