package com.mybarber.compartilhado.validacao;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/* Aceita nulo ou vazio, pois o CPF é opcional */
@Documented
@Constraint(validatedBy = CpfValidoValidator.class)
@Target({ FIELD, PARAMETER })
@Retention(RUNTIME)
public @interface CpfValido {

    String message() default "O CPF informado é inválido";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
