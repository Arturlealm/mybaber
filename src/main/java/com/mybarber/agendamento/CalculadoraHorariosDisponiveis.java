package com.mybarber.agendamento;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.mybarber.agenda.IntervaloHorario;

public final class CalculadoraHorariosDisponiveis {

    private CalculadoraHorariosDisponiveis() {
    }

    public record PeriodoOcupado(LocalDateTime inicio, LocalDateTime fim) {
    }

    /* Um horário é oferecido quando toda a duração do atendimento cabe no expediente sem sobrepor outro agendamento */
    public static List<LocalTime> calcular(
            LocalDate data,
            List<IntervaloHorario> expediente,
            int duracaoMinutos,
            int intervaloMinutos,
            List<PeriodoOcupado> ocupados,
            LocalDateTime inicioMinimoPermitido) {
        List<LocalTime> horarios = new ArrayList<>();

        for (IntervaloHorario intervalo : expediente) {
            LocalDateTime limite = data.atTime(intervalo.fim());
            for (LocalDateTime inicio = data.atTime(intervalo.inicio());
                    !inicio.plusMinutes(duracaoMinutos).isAfter(limite);
                    inicio = inicio.plusMinutes(intervaloMinutos)) {
                LocalDateTime fim = inicio.plusMinutes(duracaoMinutos);
                if (!inicio.isBefore(inicioMinimoPermitido) && estaLivre(inicio, fim, ocupados)) {
                    horarios.add(inicio.toLocalTime());
                }
            }
        }

        return horarios;
    }

    private static boolean estaLivre(LocalDateTime inicio, LocalDateTime fim, List<PeriodoOcupado> ocupados) {
        return ocupados.stream().noneMatch(ocupado -> ocupado.inicio().isBefore(fim) && ocupado.fim().isAfter(inicio));
    }
}
