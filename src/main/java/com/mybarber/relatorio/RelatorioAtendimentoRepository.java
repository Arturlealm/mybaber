package com.mybarber.relatorio;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import com.mybarber.agendamento.Agendamento;

public interface RelatorioAtendimentoRepository extends Repository<Agendamento, Long> {

    @Query("""
            select new com.mybarber.relatorio.ResumoStatusBarbeiro(
                a.funcionario.id, a.funcionario.nome, a.status, count(a),
                coalesce(sum(a.valorCobrado), 0), coalesce(sum(a.valorTabela), 0))
            from Agendamento a
            where a.filialId = :filialId and a.inicio >= :inicio and a.inicio < :fim
            group by a.funcionario.id, a.funcionario.nome, a.status
            """)
    List<ResumoStatusBarbeiro> resumirPorBarbeiroEStatus(Long filialId, LocalDateTime inicio, LocalDateTime fim);

    @Query("""
            select new com.mybarber.relatorio.QuantidadeServicoBarbeiro(
                a.funcionario.id, a.funcionario.nome, i.servicoId, i.nomeServico, count(i))
            from AgendamentoItem i join i.agendamento a
            where a.filialId = :filialId and a.inicio >= :inicio and a.inicio < :fim
              and a.status = com.mybarber.agendamento.StatusAgendamento.CONCLUIDO
            group by a.funcionario.id, a.funcionario.nome, i.servicoId, i.nomeServico
            """)
    List<QuantidadeServicoBarbeiro> contarServicosConcluidosPorBarbeiro(
            Long filialId, LocalDateTime inicio, LocalDateTime fim);
}
