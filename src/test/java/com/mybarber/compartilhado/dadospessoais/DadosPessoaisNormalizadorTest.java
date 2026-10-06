package com.mybarber.compartilhado.dadospessoais;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DadosPessoaisNormalizadorTest {

    @Test
    void deveNormalizarEmailParaMinusculasSemEspacos() {
        assertThat(DadosPessoaisNormalizador.normalizarEmail("  Joao.Silva@Email.COM ")).isEqualTo("joao.silva@email.com");
    }

    @Test
    void deveManterSomenteDigitosDoCpfEConverterVazioEmNulo() {
        assertThat(DadosPessoaisNormalizador.normalizarCpf("529.982.247-25")).isEqualTo("52998224725");
        assertThat(DadosPessoaisNormalizador.normalizarCpf("   ")).isNull();
        assertThat(DadosPessoaisNormalizador.normalizarCpf(null)).isNull();
    }

    @Test
    void deveManterSomenteDigitosDoTelefone() {
        assertThat(DadosPessoaisNormalizador.normalizarTelefone("(11) 91234-5678")).isEqualTo("11912345678");
    }

    @Test
    void deveRemoverEspacosRepetidosDoNome() {
        assertThat(DadosPessoaisNormalizador.normalizarNome("  João   da  Silva ")).isEqualTo("João da Silva");
    }

    @Test
    void deveMascararCpf() {
        assertThat(DadosPessoaisNormalizador.mascararCpf("52998224725")).isEqualTo("***.982.247-**");
        assertThat(DadosPessoaisNormalizador.mascararCpf(null)).isNull();
    }
}
