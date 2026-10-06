package com.mybarber.funcionario;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mybarber.administrador-inicial")
public record AdministradorInicialPropriedades(String nome, String email, String telefone, String senha) {
}
