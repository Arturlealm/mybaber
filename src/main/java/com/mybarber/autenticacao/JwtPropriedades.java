package com.mybarber.autenticacao;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mybarber.jwt")
public record JwtPropriedades(String segredo, Duration expiracao, String emissor) {

    private static final int TAMANHO_MINIMO_SEGREDO_BYTES = 32;

    public JwtPropriedades {
        if (segredo == null || segredo.getBytes(StandardCharsets.UTF_8).length < TAMANHO_MINIMO_SEGREDO_BYTES) {
            throw new IllegalStateException(
                    "A variável JWT_SEGREDO deve ter pelo menos " + TAMANHO_MINIMO_SEGREDO_BYTES + " caracteres");
        }
    }
}
