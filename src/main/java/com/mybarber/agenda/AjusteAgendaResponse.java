package com.mybarber.agenda;

import java.time.LocalDate;
import java.time.LocalTime;

public record AjusteAgendaResponse(
        Long id,
        LocalDate data,
        TipoAjusteAgenda tipo,
        Long filialId,
        Long funcionarioId,
        boolean todosFuncionarios,
        LocalTime horaInicio,
        LocalTime horaFim,
        String motivo) {

    public static AjusteAgendaResponse de(AjusteAgenda ajuste) {
        return new AjusteAgendaResponse(
                ajuste.getId(),
                ajuste.getData(),
                ajuste.getTipo(),
                ajuste.getFilialId(),
                ajuste.getFuncionarioId(),
                ajuste.isGeral(),
                ajuste.getHoraInicio(),
                ajuste.getHoraFim(),
                ajuste.getMotivo());
    }
}
