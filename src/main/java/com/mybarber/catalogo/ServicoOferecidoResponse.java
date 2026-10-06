package com.mybarber.catalogo;

import java.math.BigDecimal;

public record ServicoOferecidoResponse(
        Long id,
        String nome,
        String descricao,
        int duracaoMinutos,
        BigDecimal preco,
        int ordemExibicao) {

    public static ServicoOferecidoResponse de(ServicoOferecido servico) {
        return new ServicoOferecidoResponse(
                servico.getId(),
                servico.getNome(),
                servico.getDescricao(),
                servico.getDuracaoMinutos(),
                servico.getPreco(),
                servico.getOrdemExibicao());
    }
}
