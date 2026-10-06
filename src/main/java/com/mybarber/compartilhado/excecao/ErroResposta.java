package com.mybarber.compartilhado.excecao;

import java.time.Instant;
import java.util.List;

public record ErroResposta(
        int status,
        String mensagem,
        List<ErroCampoResposta> campos,
        Instant dataHora) {

    public ErroResposta(int status, String mensagem) {
        this(status, mensagem, List.of(), Instant.now());
    }

    public ErroResposta(int status, String mensagem, List<ErroCampoResposta> campos) {
        this(status, mensagem, campos, Instant.now());
    }
}
