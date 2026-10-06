package com.mybarber.agenda;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.mybarber.compartilhado.excecao.RegraNegocioException;

final class ValidadorIntervalosJornada {

    private ValidadorIntervalosJornada() {
    }

    static void validar(List<JornadaSemanalIntervaloRequest> intervalos) {
        for (JornadaSemanalIntervaloRequest intervalo : intervalos) {
            if (!intervalo.horaFim().isAfter(intervalo.horaInicio())) {
                throw new RegraNegocioException("A hora de fim deve ser posterior à hora de início em "
                        + intervalo.diaSemana());
            }
        }

        Map<DiaSemana, List<JornadaSemanalIntervaloRequest>> intervalosPorDia = intervalos.stream()
                .collect(Collectors.groupingBy(JornadaSemanalIntervaloRequest::diaSemana));

        intervalosPorDia.forEach((dia, intervalosDoDia) -> {
            List<JornadaSemanalIntervaloRequest> ordenados = intervalosDoDia.stream()
                    .sorted(Comparator.comparing(JornadaSemanalIntervaloRequest::horaInicio))
                    .toList();
            for (int i = 1; i < ordenados.size(); i++) {
                if (ordenados.get(i).horaInicio().isBefore(ordenados.get(i - 1).horaFim())) {
                    throw new RegraNegocioException("Existem intervalos sobrepostos em " + dia);
                }
            }
        });
    }
}
