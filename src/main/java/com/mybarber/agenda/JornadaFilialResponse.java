package com.mybarber.agenda;

import java.util.Comparator;
import java.util.List;

public record JornadaFilialResponse(Long filialId, List<JornadaFuncionarioResponse.Intervalo> intervalos) {

    public static JornadaFilialResponse de(Long filialId, List<JornadaFilial> jornadas) {
        List<JornadaFuncionarioResponse.Intervalo> intervalos = jornadas.stream()
                .sorted(Comparator.comparing(JornadaFilial::getDiaSemana).thenComparing(JornadaFilial::getHoraInicio))
                .map(jornada -> new JornadaFuncionarioResponse.Intervalo(
                        jornada.getDiaSemana(), jornada.getHoraInicio(), jornada.getHoraFim()))
                .toList();
        return new JornadaFilialResponse(filialId, intervalos);
    }
}
