package com.mybarber.autenticacao;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

class ControleTentativasLoginTest {

    private static final String CHAVE = "CLIENTE:joao@email.com";

    private final RelogioAjustavel relogio = new RelogioAjustavel();
    private final ControleTentativasLogin controle = new ControleTentativasLogin(
            new TentativasLoginPropriedades(5, Duration.ofMinutes(15)), relogio);

    @Test
    void deveBloquearAposCincoFalhasSeguidas() {
        registrarFalhas(5);

        assertThatThrownBy(() -> controle.verificarBloqueio(CHAVE))
                .isInstanceOf(MuitasTentativasLoginException.class)
                .hasMessageContaining("15 minuto(s)");
    }

    @Test
    void naoDeveBloquearAntesDoLimite() {
        registrarFalhas(4);

        assertThatCode(() -> controle.verificarBloqueio(CHAVE)).doesNotThrowAnyException();
    }

    @Test
    void deveLiberarDepoisDoTempoDeBloqueio() {
        registrarFalhas(5);
        relogio.avancar(Duration.ofMinutes(15).plusSeconds(1));

        assertThatCode(() -> controle.verificarBloqueio(CHAVE)).doesNotThrowAnyException();
    }

    @Test
    void loginComSucessoDeveZerarAsFalhas() {
        registrarFalhas(4);
        controle.registrarSucesso(CHAVE);
        registrarFalhas(4);

        assertThatCode(() -> controle.verificarBloqueio(CHAVE)).doesNotThrowAnyException();
    }

    @Test
    void falhasForaDaJanelaNaoDevemSerSomadas() {
        registrarFalhas(4);
        relogio.avancar(Duration.ofMinutes(16));
        registrarFalhas(1);

        assertThatCode(() -> controle.verificarBloqueio(CHAVE)).doesNotThrowAnyException();
    }

    private void registrarFalhas(int quantidade) {
        for (int i = 0; i < quantidade; i++) {
            controle.registrarFalha(CHAVE);
        }
    }

    private static final class RelogioAjustavel extends Clock {

        private Instant agora = Instant.parse("2026-10-06T12:00:00Z");

        void avancar(Duration duracao) {
            agora = agora.plus(duracao);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zona) {
            return this;
        }

        @Override
        public Instant instant() {
            return agora;
        }
    }
}
