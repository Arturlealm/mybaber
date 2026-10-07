package com.mybarber.autenticacao;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/* Controle em memória: em mais de uma instância da API, cada uma conta as tentativas separadamente */
@Component
public class ControleTentativasLogin {

    private record RegistroTentativas(int falhas, Instant inicioJanela, Instant bloqueadoAte) {
    }

    private final Map<String, RegistroTentativas> registros = new ConcurrentHashMap<>();
    private final TentativasLoginPropriedades propriedades;
    private final Clock relogio;

    public ControleTentativasLogin(TentativasLoginPropriedades propriedades, Clock relogio) {
        this.propriedades = propriedades;
        this.relogio = relogio;
    }

    public void verificarBloqueio(String chave) {
        RegistroTentativas registro = registros.get(chave);
        Instant agora = relogio.instant();
        if (registro == null || registro.bloqueadoAte() == null) {
            return;
        }
        if (agora.isBefore(registro.bloqueadoAte())) {
            long minutosRestantes = Math.max(1, (Duration.between(agora, registro.bloqueadoAte()).toSeconds() + 59) / 60);
            throw new MuitasTentativasLoginException(minutosRestantes);
        }
        registros.remove(chave);
    }

    public void registrarFalha(String chave) {
        Instant agora = relogio.instant();
        registros.compute(chave, (ignorada, atual) -> {
            boolean janelaExpirada = atual == null
                    || agora.isAfter(atual.inicioJanela().plus(propriedades.tempoBloqueio()));
            int falhas = janelaExpirada ? 1 : atual.falhas() + 1;
            Instant inicioJanela = janelaExpirada ? agora : atual.inicioJanela();
            Instant bloqueadoAte = falhas >= propriedades.maximoTentativas()
                    ? agora.plus(propriedades.tempoBloqueio())
                    : null;
            return new RegistroTentativas(falhas, inicioJanela, bloqueadoAte);
        });
    }

    public void registrarSucesso(String chave) {
        registros.remove(chave);
    }
}
