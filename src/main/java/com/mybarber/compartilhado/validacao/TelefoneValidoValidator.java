package com.mybarber.compartilhado.validacao;

import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TelefoneValidoValidator implements ConstraintValidator<TelefoneValido, String> {

    private static final Pattern CARACTERES_PERMITIDOS = Pattern.compile("^[\\d\\s()+-]+$");

    @Override
    public boolean isValid(String valor, ConstraintValidatorContext contexto) {
        if (valor == null || valor.isBlank()) {
            return true;
        }

        if (!CARACTERES_PERMITIDOS.matcher(valor.strip()).matches()) {
            return false;
        }

        int quantidadeDigitos = valor.replaceAll("\\D", "").length();
        return quantidadeDigitos == 10 || quantidadeDigitos == 11;
    }
}
