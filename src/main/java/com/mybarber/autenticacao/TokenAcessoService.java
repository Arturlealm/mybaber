package com.mybarber.autenticacao;

import java.time.Instant;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class TokenAcessoService {

    private final JwtEncoder jwtEncoder;
    private final JwtPropriedades jwtPropriedades;

    public TokenAcessoService(JwtEncoder jwtEncoder, JwtPropriedades jwtPropriedades) {
        this.jwtEncoder = jwtEncoder;
        this.jwtPropriedades = jwtPropriedades;
    }

    public TokenAcessoResponse gerar(Long idUsuario, String nome, PerfilAcesso perfil) {
        Instant agora = Instant.now();
        Instant expiraEm = agora.plus(jwtPropriedades.expiracao());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtPropriedades.emissor())
                .issuedAt(agora)
                .expiresAt(expiraEm)
                .subject(idUsuario.toString())
                .claim(SegurancaConfig.CLAIM_PERFIL, perfil.name())
                .claim(SegurancaConfig.CLAIM_NOME, nome)
                .build();

        JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(cabecalho, claims)).getTokenValue();

        return new TokenAcessoResponse(token, "Bearer", expiraEm, perfil, nome);
    }
}
