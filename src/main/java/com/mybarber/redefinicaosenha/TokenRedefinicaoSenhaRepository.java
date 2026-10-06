package com.mybarber.redefinicaosenha;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface TokenRedefinicaoSenhaRepository extends JpaRepository<TokenRedefinicaoSenha, Long> {

    Optional<TokenRedefinicaoSenha> findByTokenHash(String tokenHash);

    boolean existsByTipoContaAndUsuarioIdAndCriadoEmAfter(TipoConta tipoConta, Long usuarioId, Instant criadoApos);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update TokenRedefinicaoSenha t set t.usadoEm = :agora
            where t.tipoConta = :tipoConta and t.usuarioId = :usuarioId and t.usadoEm is null
            """)
    void invalidarTokensAbertos(TipoConta tipoConta, Long usuarioId, Instant agora);
}
