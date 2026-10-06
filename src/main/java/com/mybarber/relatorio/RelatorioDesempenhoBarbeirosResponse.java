package com.mybarber.relatorio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RelatorioDesempenhoBarbeirosResponse(
        LocalDate inicio,
        LocalDate fim,
        Long filialId,
        Totais totais,
        Destaque maisAtendimentos,
        Destaque maiorFaturamento,
        List<Destaque> maisAtendimentosPorServico,
        List<Barbeiro> barbeiros) {

    public record Totais(long atendimentosConcluidos, BigDecimal faturamento, BigDecimal descontos) {
    }

    public record Destaque(
            Long funcionarioId,
            String nomeFuncionario,
            Long servicoId,
            String nomeServico,
            long quantidade,
            BigDecimal valor) {
    }

    public record Barbeiro(
            Long funcionarioId,
            String nome,
            long atendimentosConcluidos,
            BigDecimal faturamento,
            BigDecimal descontos,
            BigDecimal ticketMedio,
            long naoComparecimentos,
            long cancelamentos,
            long agendados,
            List<Servico> servicos) {
    }

    public record Servico(Long servicoId, String nome, long quantidade) {
    }
}
