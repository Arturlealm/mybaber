package com.mybarber.lembrete;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mybarber.lembrete")
public record LembreteAgendamentoPropriedades(
        boolean habilitado,
        Duration antecedencia,
        Duration antecedenciaMinimaDesdeCriacao,
        int limiteDiario,
        String urlFrontend) {
}
