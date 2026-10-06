package com.mybarber.filial;

import com.mybarber.compartilhado.validacao.TelefoneValido;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FilialCadastroRequest(
        @NotBlank(message = "O nome da filial é obrigatório")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
        String nome,

        @TelefoneValido
        String telefone,

        @Size(max = 255, message = "O endereço deve ter no máximo 255 caracteres")
        String endereco) {
}
