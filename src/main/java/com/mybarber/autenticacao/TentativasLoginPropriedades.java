package com.mybarber.autenticacao;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mybarber.autenticacao")
public record TentativasLoginPropriedades(int maximoTentativas, Duration tempoBloqueio) {
}
