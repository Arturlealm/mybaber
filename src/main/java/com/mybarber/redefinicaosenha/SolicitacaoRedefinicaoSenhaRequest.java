package com.mybarber.redefinicaosenha;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SolicitacaoRedefinicaoSenhaRequest(
        @NotBlank(message = "O email é obrigatório")
        @Email(message = "O email deve ser válido")
        String email,

        @NotNull(message = "Informe se a conta é de CLIENTE ou FUNCIONARIO")
        TipoConta tipoConta) {
}
