package com.mybarber.agenda;

import java.util.Comparator;
import java.util.List;

public record ExpedienteDia(
        SituacaoExpediente situacao,
        OrigemExpediente origem,
        Long ajusteId,
        String motivo,
        List<IntervaloHorario> intervalos) {

    /* Prioridade: ajuste do funcionário, depois ajuste geral da filial, depois a jornada semanal */
    public static ExpedienteDia calcular(
            List<JornadaFuncionario> jornadasDoDia,
            AjusteAgenda ajusteFuncionario,
            AjusteAgenda ajusteGeral) {
        if (ajusteFuncionario != null) {
            return deAjuste(ajusteFuncionario, OrigemExpediente.AJUSTE_FUNCIONARIO);
        }
        if (ajusteGeral != null) {
            return deAjuste(ajusteGeral, OrigemExpediente.AJUSTE_GERAL);
        }
        if (jornadasDoDia.isEmpty()) {
            return new ExpedienteDia(SituacaoExpediente.FECHADO, OrigemExpediente.JORNADA, null, null, List.of());
        }

        List<IntervaloHorario> intervalos = jornadasDoDia.stream()
                .sorted(Comparator.comparing(JornadaFuncionario::getHoraInicio))
                .map(jornada -> new IntervaloHorario(jornada.getHoraInicio(), jornada.getHoraFim()))
                .toList();
        return new ExpedienteDia(SituacaoExpediente.ABERTO, OrigemExpediente.JORNADA, null, null, intervalos);
    }

    private static ExpedienteDia deAjuste(AjusteAgenda ajuste, OrigemExpediente origem) {
        if (ajuste.getTipo() == TipoAjusteAgenda.FECHADO) {
            return new ExpedienteDia(SituacaoExpediente.FECHADO, origem, ajuste.getId(), ajuste.getMotivo(), List.of());
        }
        return new ExpedienteDia(
                SituacaoExpediente.ABERTO,
                origem,
                ajuste.getId(),
                ajuste.getMotivo(),
                List.of(new IntervaloHorario(ajuste.getHoraInicio(), ajuste.getHoraFim())));
    }
}
