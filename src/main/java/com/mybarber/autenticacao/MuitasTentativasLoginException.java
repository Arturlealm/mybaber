package com.mybarber.autenticacao;

public class MuitasTentativasLoginException extends RuntimeException {

    public MuitasTentativasLoginException(long minutosRestantes) {
        super("Muitas tentativas de login. Tente novamente em " + minutosRestantes + " minuto(s)");
    }
}
