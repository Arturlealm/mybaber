package com.mybarber.autenticacao;

import org.springframework.security.oauth2.jwt.Jwt;

public record UsuarioAutenticado(Long id, PerfilAcesso perfil) {

    public static UsuarioAutenticado de(Jwt jwt) {
        return new UsuarioAutenticado(
                Long.valueOf(jwt.getSubject()),
                PerfilAcesso.valueOf(jwt.getClaimAsString(SegurancaConfig.CLAIM_PERFIL)));
    }

    public boolean isCliente() {
        return perfil == PerfilAcesso.CLIENTE;
    }

    public boolean isBarbeiro() {
        return perfil == PerfilAcesso.BARBEIRO;
    }

    public boolean isAdministrador() {
        return perfil == PerfilAcesso.ADMINISTRADOR;
    }
}
