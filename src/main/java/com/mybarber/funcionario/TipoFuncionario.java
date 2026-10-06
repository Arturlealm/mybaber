package com.mybarber.funcionario;

import com.mybarber.autenticacao.PerfilAcesso;

public enum TipoFuncionario {
    BARBEIRO(PerfilAcesso.BARBEIRO),
    ADMINISTRADOR(PerfilAcesso.ADMINISTRADOR);

    private final PerfilAcesso perfilAcesso;

    TipoFuncionario(PerfilAcesso perfilAcesso) {
        this.perfilAcesso = perfilAcesso;
    }

    public PerfilAcesso getPerfilAcesso() {
        return perfilAcesso;
    }
}
