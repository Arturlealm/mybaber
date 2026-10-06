package com.mybarber.agendamento;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/* clienteId é obrigatório apenas quando um funcionário agenda em nome do cliente */
public record AgendamentoCriacaoRequest(
        @NotNull(message = "O barbeiro é obrigatório")
        Long funcionarioId,

        @NotNull(message = "O horário de início é obrigatório")
        LocalDateTime inicio,

        @NotEmpty(message = "Selecione ao menos um serviço")
        List<Long> servicoIds,

        Long clienteId) {
}
