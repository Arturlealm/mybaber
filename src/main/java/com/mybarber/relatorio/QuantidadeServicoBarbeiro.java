package com.mybarber.relatorio;

public record QuantidadeServicoBarbeiro(
        Long funcionarioId,
        String nomeFuncionario,
        Long servicoId,
        String nomeServico,
        Long quantidade) {
}
