package com.mybarber.relatorio;

import java.math.BigDecimal;

import com.mybarber.agendamento.StatusAgendamento;

public record ResumoStatusBarbeiro(
        Long funcionarioId,
        String nomeFuncionario,
        StatusAgendamento status,
        Long quantidade,
        BigDecimal valorCobrado,
        BigDecimal valorTabela) {
}
