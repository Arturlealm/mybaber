package com.mybarber.redefinicaosenha;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tokens_redefinicao_senha")
public class TokenRedefinicaoSenha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_hash", nullable = false, length = 64, unique = true)
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_conta", nullable = false, length = 20)
    private TipoConta tipoConta;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "expira_em", nullable = false)
    private Instant expiraEm;

    @Column(name = "usado_em")
    private Instant usadoEm;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    protected TokenRedefinicaoSenha() {
    }

    public TokenRedefinicaoSenha(String tokenHash, TipoConta tipoConta, Long usuarioId, Instant expiraEm) {
        this.tokenHash = tokenHash;
        this.tipoConta = tipoConta;
        this.usuarioId = usuarioId;
        this.expiraEm = expiraEm;
    }

    public boolean podeSerUsado(Instant agora) {
        return usadoEm == null && agora.isBefore(expiraEm);
    }

    public void marcarComoUsado(Instant agora) {
        this.usadoEm = agora;
    }

    public Long getId() {
        return id;
    }

    public TipoConta getTipoConta() {
        return tipoConta;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }
}
