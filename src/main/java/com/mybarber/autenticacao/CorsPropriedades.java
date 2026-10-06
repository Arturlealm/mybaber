package com.mybarber.autenticacao;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mybarber.cors")
public record CorsPropriedades(List<String> origensPermitidas) {
}
