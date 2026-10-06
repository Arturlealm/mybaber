package com.mybarber.catalogo;

import java.math.BigDecimal;
import java.util.List;

public record CombinacaoServicoResponse(
        Long id,
        String nome,
        String descricao,
        int duracaoTotalMinutos,
        BigDecimal precoTotal,
        List<ServicoOferecidoResponse> servicos) {

    public static CombinacaoServicoResponse de(CombinacaoServico combinacao) {
        return new CombinacaoServicoResponse(
                combinacao.getId(),
                combinacao.getNome(),
                combinacao.getDescricao(),
                combinacao.calcularDuracaoTotalMinutos(),
                combinacao.calcularPrecoTotal(),
                combinacao.getServicos().stream().map(ServicoOferecidoResponse::de).toList());
    }
}
