package com.mybarber.agenda;

import java.time.DayOfWeek;
import java.time.LocalDate;

public enum DiaSemana {
    SEGUNDA(DayOfWeek.MONDAY),
    TERCA(DayOfWeek.TUESDAY),
    QUARTA(DayOfWeek.WEDNESDAY),
    QUINTA(DayOfWeek.THURSDAY),
    SEXTA(DayOfWeek.FRIDAY),
    SABADO(DayOfWeek.SATURDAY),
    DOMINGO(DayOfWeek.SUNDAY);

    private final DayOfWeek diaDaSemanaJava;

    DiaSemana(DayOfWeek diaDaSemanaJava) {
        this.diaDaSemanaJava = diaDaSemanaJava;
    }

    public static DiaSemana de(LocalDate data) {
        DayOfWeek diaDaSemana = data.getDayOfWeek();
        for (DiaSemana dia : values()) {
            if (dia.diaDaSemanaJava == diaDaSemana) {
                return dia;
            }
        }
        throw new IllegalStateException("Dia da semana desconhecido: " + diaDaSemana);
    }
}
