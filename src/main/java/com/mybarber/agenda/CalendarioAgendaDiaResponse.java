package com.mybarber.agenda;

import java.time.LocalDate;
import java.util.List;

public record CalendarioAgendaDiaResponse(
        LocalDate data,
        DiaSemana diaSemana,
        AjusteAgendaResponse ajusteGeral,
        List<Funcionario> funcionarios) {

    public record Funcionario(
            Long funcionarioId,
            String nome,
            SituacaoExpediente situacao,
            OrigemExpediente origem,
            Long ajusteId,
            String motivo,
            List<IntervaloHorario> intervalos) {
    }
}
