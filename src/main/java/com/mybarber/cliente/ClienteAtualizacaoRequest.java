package com.mybarber.cliente;

import com.mybarber.compartilhado.validacao.CpfValido;
import com.mybarber.compartilhado.validacao.TelefoneValido;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClienteAtualizacaoRequest(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
        String nome,

        @NotBlank(message = "O email é obrigatório")
        @Email(message = "O email deve ser válido")
        @Size(max = 254, message = "O email deve ter no máximo 254 caracteres")
        String email,

        @CpfValido
        String cpf,

        @NotBlank(message = "O telefone é obrigatório")
        @TelefoneValido
        String telefone) {
}
