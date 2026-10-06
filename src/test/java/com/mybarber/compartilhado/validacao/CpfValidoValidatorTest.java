package com.mybarber.compartilhado.validacao;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CpfValidoValidatorTest {

    private final CpfValidoValidator validador = new CpfValidoValidator();

    @ParameterizedTest
    @ValueSource(strings = { "529.982.247-25", "52998224725", " 529.982.247-25 " })
    void deveAceitarCpfValidoComOuSemFormatacao(String cpf) {
        assertThat(validador.isValid(cpf, null)).isTrue();
    }

    @Test
    void deveAceitarCpfNuloOuVazioPoisEhOpcional() {
        assertThat(validador.isValid(null, null)).isTrue();
        assertThat(validador.isValid("  ", null)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = { "529.982.247-26", "111.111.111-11", "1234567890", "529-982-247.25", "abc" })
    void deveRejeitarCpfInvalido(String cpf) {
        assertThat(validador.isValid(cpf, null)).isFalse();
    }
}
