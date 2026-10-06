package com.mybarber.agenda;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record JornadaSemanalRequest(
        @NotNull(message = "Informe os intervalos da jornada")
        List<@Valid JornadaSemanalIntervaloRequest> intervalos) {
}
