package com.mybarber.cliente;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mybarber.cliente")
public record ClienteBalcaoPropriedades(String senhaPadraoBalcao) {
}
