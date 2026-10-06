package com.mybarber.funcionario;

import java.time.Instant;

import com.mybarber.compartilhado.dadospessoais.DadosPessoaisNormalizador;

public record FuncionarioResponse(
        Long id,
        String nome,
        String email,
        String cpf,
        String telefone,
        TipoFuncionario tipo,
        boolean realizaAtendimentos,
        boolean ativo,
        Instant criadoEm) {

    public static FuncionarioResponse de(Funcionario funcionario) {
        return new FuncionarioResponse(
                funcionario.getId(),
                funcionario.getNome(),
                funcionario.getEmail(),
                DadosPessoaisNormalizador.mascararCpf(funcionario.getCpf()),
                funcionario.getTelefone(),
                funcionario.getTipo(),
                funcionario.isRealizaAtendimentos(),
                funcionario.isAtivo(),
                funcionario.getCriadoEm());
    }
}
