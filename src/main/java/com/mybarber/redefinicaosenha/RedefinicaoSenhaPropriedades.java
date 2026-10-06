package com.mybarber.redefinicaosenha;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mybarber.redefinicao-senha")
public record RedefinicaoSenhaPropriedades(Duration validade, Duration intervaloMinimoEntreSolicitacoes, String urlFrontend) {
}
