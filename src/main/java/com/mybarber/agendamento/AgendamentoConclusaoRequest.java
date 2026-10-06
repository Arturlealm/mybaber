package com.mybarber.agendamento;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;

/* valorCobrado nulo confirma o valor de tabela; informado, registra o valor realmente cobrado (ex.: desconto) */
public record AgendamentoConclusaoRequest(
        @DecimalMin(value = "0.00", message = "O valor cobrado não pode ser negativo")
        @Digits(integer = 8, fraction = 2, message = "O valor cobrado deve ter no máximo 2 casas decimais")
        BigDecimal valorCobrado,

        @Size(max = 255, message = "A observação deve ter no máximo 255 caracteres")
        String observacao) {
}
