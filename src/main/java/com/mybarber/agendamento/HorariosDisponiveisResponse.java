package com.mybarber.agendamento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record HorariosDisponiveisResponse(
        LocalDate data,
        Long funcionarioId,
        int duracaoTotalMinutos,
        BigDecimal valorTabela,
        List<LocalTime> horarios) {
}
