package com.mybarber.catalogo;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ServicoOferecidoRequest(
        @NotBlank(message = "O nome do serviço é obrigatório")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
        String nome,

        @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres")
        String descricao,

        @NotNull(message = "A duração é obrigatória")
        @Min(value = 0, message = "A duração não pode ser negativa")
        @Max(value = 480, message = "A duração deve ser de no máximo 480 minutos")
        Integer duracaoMinutos,

        @NotNull(message = "O preço é obrigatório")
        @DecimalMin(value = "0.00", message = "O preço não pode ser negativo")
        @Digits(integer = 8, fraction = 2, message = "O preço deve ter no máximo 2 casas decimais")
        BigDecimal preco,

        Integer ordemExibicao) {
}
