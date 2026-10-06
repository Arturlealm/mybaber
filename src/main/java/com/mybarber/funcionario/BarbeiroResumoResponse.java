package com.mybarber.funcionario;

public record BarbeiroResumoResponse(Long id, String nome, Long filialId) {

    public static BarbeiroResumoResponse de(Funcionario funcionario) {
        return new BarbeiroResumoResponse(funcionario.getId(), funcionario.getNome(), funcionario.getFilial().getId());
    }
}
