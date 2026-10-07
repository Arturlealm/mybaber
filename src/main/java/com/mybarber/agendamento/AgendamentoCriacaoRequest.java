package com.mybarber.agendamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/* clienteId e os dados de atendimento realizado são usados apenas quando a equipe registra pelo balcão */
public record AgendamentoCriacaoRequest(
        @NotNull(message = "O barbeiro é obrigatório")
        Long funcionarioId,

        @NotNull(message = "O horário de início é obrigatório")
        LocalDateTime inicio,

        @NotEmpty(message = "Selecione ao menos um serviço")
        List<Long> servicoIds,

        Long clienteId,

        Boolean atendimentoRealizado,

        @DecimalMin(value = "0.00", message = "O valor cobrado não pode ser negativo")
        @Digits(integer = 8, fraction = 2, message = "O valor cobrado deve ter no máximo 2 casas decimais")
        BigDecimal valorCobrado,

        @Size(max = 255, message = "A observação deve ter no máximo 255 caracteres")
        String observacao) {

    public boolean registrarComoRealizado() {
        return Boolean.TRUE.equals(atendimentoRealizado);
    }
}
