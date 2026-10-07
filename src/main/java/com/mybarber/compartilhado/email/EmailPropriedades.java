package com.mybarber.compartilhado.email;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mybarber.email")
public record EmailPropriedades(boolean habilitado, String remetente, String nomeRemetente) {
}
