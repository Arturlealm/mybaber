package com.mybarber.agendamento;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mybarber.agenda")
public record AgendaPropriedades(
        int intervaloMinutos,
        Duration antecedenciaMinimaAgendamento,
        Duration antecedenciaMinimaCancelamentoCliente,
        int diasMaximosAntecedencia,
        int diasMaximosRetroativosAdministrador) {
}
