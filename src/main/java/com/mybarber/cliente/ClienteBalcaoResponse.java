package com.mybarber.cliente;

/* senhaInicial é devolvida para o administrador informar ao cliente no atendimento */
public record ClienteBalcaoResponse(ClienteResponse cliente, String senhaInicial) {
}
