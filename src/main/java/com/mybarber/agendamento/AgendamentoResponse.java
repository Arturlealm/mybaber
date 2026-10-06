package com.mybarber.agendamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AgendamentoResponse(
        Long id,
        Cliente cliente,
        Barbeiro barbeiro,
        Long filialId,
        LocalDateTime inicio,
        LocalDateTime fim,
        StatusAgendamento status,
        List<Item> itens,
        BigDecimal valorTabela,
        BigDecimal valorCobrado,
        BigDecimal desconto,
        String observacao,
        OrigemCancelamento canceladoPor,
        String motivoCancelamento) {

    public record Cliente(Long id, String nome, String telefone) {
    }

    public record Barbeiro(Long id, String nome) {
    }

    public record Item(Long servicoId, String nome, int duracaoMinutos, BigDecimal precoTabela) {
    }

    public static AgendamentoResponse de(Agendamento agendamento) {
        BigDecimal desconto = agendamento.getValorCobrado() == null
                ? null
                : agendamento.getValorTabela().subtract(agendamento.getValorCobrado()).max(BigDecimal.ZERO);

        return new AgendamentoResponse(
                agendamento.getId(),
                new Cliente(
                        agendamento.getCliente().getId(),
                        agendamento.getCliente().getNome(),
                        agendamento.getCliente().getTelefone()),
                new Barbeiro(agendamento.getFuncionario().getId(), agendamento.getFuncionario().getNome()),
                agendamento.getFilialId(),
                agendamento.getInicio(),
                agendamento.getFim(),
                agendamento.getStatus(),
                agendamento.getItens().stream()
                        .map(item -> new Item(
                                item.getServicoId(), item.getNomeServico(), item.getDuracaoMinutos(), item.getPrecoTabela()))
                        .toList(),
                agendamento.getValorTabela(),
                agendamento.getValorCobrado(),
                desconto,
                agendamento.getObservacao(),
                agendamento.getCanceladoPor(),
                agendamento.getMotivoCancelamento());
    }
}
