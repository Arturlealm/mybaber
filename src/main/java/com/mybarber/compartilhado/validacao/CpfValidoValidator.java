package com.mybarber.compartilhado.validacao;

import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CpfValidoValidator implements ConstraintValidator<CpfValido, String> {

    private static final Pattern FORMATO_CPF = Pattern.compile("^\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}$");

    @Override
    public boolean isValid(String valor, ConstraintValidatorContext contexto) {
        if (valor == null || valor.isBlank()) {
            return true;
        }

        String cpf = valor.strip();
        if (!FORMATO_CPF.matcher(cpf).matches()) {
            return false;
        }

        String digitos = cpf.replaceAll("\\D", "");
        if (digitos.chars().distinct().count() == 1) {
            return false;
        }

        return calcularDigitoVerificador(digitos, 9) == digitos.charAt(9) - '0'
                && calcularDigitoVerificador(digitos, 10) == digitos.charAt(10) - '0';
    }

    private int calcularDigitoVerificador(String digitos, int quantidadeDigitos) {
        int soma = 0;
        for (int i = 0; i < quantidadeDigitos; i++) {
            soma += (digitos.charAt(i) - '0') * (quantidadeDigitos + 1 - i);
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
