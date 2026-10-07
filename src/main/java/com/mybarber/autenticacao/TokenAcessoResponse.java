package com.mybarber.autenticacao;

import java.time.Instant;

public record TokenAcessoResponse(
        String tokenAcesso,
        String tipoToken,
        Instant expiraEm,
        PerfilAcesso perfil,
        String nome,
        boolean usaSenhaPadrao) {
}
