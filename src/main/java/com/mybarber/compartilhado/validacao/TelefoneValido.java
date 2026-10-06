package com.mybarber.compartilhado.validacao;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = TelefoneValidoValidator.class)
@Target({ FIELD, PARAMETER })
@Retention(RUNTIME)
public @interface TelefoneValido {

    String message() default "O telefone deve conter DDD e número, por exemplo (11) 91234-5678";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
