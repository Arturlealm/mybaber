package com.mybarber.agendamento;

import jakarta.validation.constraints.Size;

public record AgendamentoCancelamentoRequest(
        @Size(max = 255, message = "O motivo deve ter no máximo 255 caracteres")
        String motivo) {
}
