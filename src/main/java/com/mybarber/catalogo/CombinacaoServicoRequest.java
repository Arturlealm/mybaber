package com.mybarber.catalogo;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record CombinacaoServicoRequest(
        @NotBlank(message = "O nome da combinação é obrigatório")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
        String nome,

        @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres")
        String descricao,

        @NotEmpty(message = "Informe ao menos um serviço")
        List<Long> servicoIds,

        Integer ordemExibicao) {
}
