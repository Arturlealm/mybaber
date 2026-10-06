package com.mybarber.cliente;

import java.time.Instant;

import com.mybarber.compartilhado.dadospessoais.DadosPessoaisNormalizador;

public record ClienteResponse(
        Long id,
        String nome,
        String email,
        String cpf,
        String telefone,
        boolean ativo,
        Instant criadoEm) {

    public static ClienteResponse de(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getNome(),
                cliente.getEmail(),
                DadosPessoaisNormalizador.mascararCpf(cliente.getCpf()),
                cliente.getTelefone(),
                cliente.isAtivo(),
                cliente.getCriadoEm());
    }
}
