package com.mybarber.agenda;

import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;

public record JornadaSemanalIntervaloRequest(
        @NotNull(message = "O dia da semana é obrigatório")
        DiaSemana diaSemana,

        @NotNull(message = "A hora de início é obrigatória")
        LocalTime horaInicio,

        @NotNull(message = "A hora de fim é obrigatória")
        LocalTime horaFim) {
}
