package com.mybarber.compartilhado.dadospessoais;

import java.util.Locale;

public final class DadosPessoaisNormalizador {

    private DadosPessoaisNormalizador() {
    }

    public static String normalizarNome(String nome) {
        return nome == null ? null : nome.strip().replaceAll("\\s+", " ");
    }

    public static String normalizarEmail(String email) {
        return email == null ? null : email.strip().toLowerCase(Locale.ROOT);
    }

    public static String normalizarCpf(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            return null;
        }
        return cpf.replaceAll("\\D", "");
    }

    public static String normalizarTelefone(String telefone) {
        return telefone == null ? null : telefone.replaceAll("\\D", "");
    }

    public static String mascararCpf(String cpf) {
        if (cpf == null || cpf.length() != 11) {
            return null;
        }
        return "***." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-**";
    }
}
