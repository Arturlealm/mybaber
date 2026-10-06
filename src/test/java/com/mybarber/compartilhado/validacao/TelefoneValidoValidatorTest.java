package com.mybarber.compartilhado.validacao;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TelefoneValidoValidatorTest {

    private final TelefoneValidoValidator validador = new TelefoneValidoValidator();

    @ParameterizedTest
    @ValueSource(strings = { "(11) 91234-5678", "11912345678", "(11) 3456-7890", "1134567890" })
    void deveAceitarTelefoneComDdd(String telefone) {
        assertThat(validador.isValid(telefone, null)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = { "91234-5678", "119123456789", "11 9abc-5678" })
    void deveRejeitarTelefoneInvalido(String telefone) {
        assertThat(validador.isValid(telefone, null)).isFalse();
    }
}
