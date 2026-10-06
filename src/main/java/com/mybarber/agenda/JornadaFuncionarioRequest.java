package com.mybarber.agenda;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record JornadaFuncionarioRequest(
        @NotNull(message = "Informe os intervalos da jornada, ou uma lista vazia para nenhum dia de trabalho")
        List<@Valid JornadaFuncionarioIntervaloRequest> intervalos) {
}
