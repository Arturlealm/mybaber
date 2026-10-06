package com.mybarber.filial;

public record FilialResponse(Long id, String nome, String telefone, String endereco, boolean ativo) {

    public static FilialResponse de(Filial filial) {
        return new FilialResponse(
                filial.getId(), filial.getNome(), filial.getTelefone(), filial.getEndereco(), filial.isAtivo());
    }
}
