package com.mybarber.agenda;

import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

public record JornadaFuncionarioResponse(Long funcionarioId, List<Intervalo> intervalos) {

    public record Intervalo(DiaSemana diaSemana, LocalTime horaInicio, LocalTime horaFim) {
    }

    public static JornadaFuncionarioResponse de(Long funcionarioId, List<JornadaFuncionario> jornadas) {
        List<Intervalo> intervalos = jornadas.stream()
                .sorted(Comparator.comparing(JornadaFuncionario::getDiaSemana)
                        .thenComparing(JornadaFuncionario::getHoraInicio))
                .map(jornada -> new Intervalo(jornada.getDiaSemana(), jornada.getHoraInicio(), jornada.getHoraFim()))
                .toList();
        return new JornadaFuncionarioResponse(funcionarioId, intervalos);
    }
}
