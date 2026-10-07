package com.mybarber.compartilhado.email;

public record MensagemEmail(String destinatario, String assunto, String textoSimples, String html) {
}
