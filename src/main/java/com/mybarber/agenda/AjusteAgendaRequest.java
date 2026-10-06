package com.mybarber.agenda;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/* funcionarioId nulo aplica o ajuste a todos os barbeiros da filial */
public record AjusteAgendaRequest(
        @NotNull(message = "A data é obrigatória")
        LocalDate data,

        @NotNull(message = "Informe se a agenda será ABERTO ou FECHADO")
        TipoAjusteAgenda tipo,

        Long funcionarioId,

        Long filialId,

        LocalTime horaInicio,

        LocalTime horaFim,

        @Size(max = 255, message = "O motivo deve ter no máximo 255 caracteres")
        String motivo) {
}
