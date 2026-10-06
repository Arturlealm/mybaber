package com.mybarber.agendamento;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    @Query("""
            select a from Agendamento a
            where a.funcionario.id = :funcionarioId
              and a.status in :status
              and a.inicio < :fim and a.fim > :inicio
            order by a.inicio
            """)
    List<Agendamento> buscarDoFuncionarioNoPeriodo(
            Long funcionarioId, LocalDateTime inicio, LocalDateTime fim, List<StatusAgendamento> status);

    @Query("""
            select a from Agendamento a
            where a.filialId = :filialId
              and a.inicio >= :inicio and a.inicio < :fim
            order by a.funcionario.nome, a.inicio
            """)
    List<Agendamento> buscarDaFilialNoPeriodo(Long filialId, LocalDateTime inicio, LocalDateTime fim);

    @Query("""
            select count(a) > 0 from Agendamento a
            where a.cliente.id = :clienteId
              and a.status = com.mybarber.agendamento.StatusAgendamento.AGENDADO
              and a.inicio < :fim and a.fim > :inicio
            """)
    boolean existeAgendamentoDoClienteNoPeriodo(Long clienteId, LocalDateTime inicio, LocalDateTime fim);

    Page<Agendamento> findAllByClienteIdOrderByInicioDesc(Long clienteId, Pageable paginacao);

    @Query("""
            select a from Agendamento a
            where a.filialId = :filialId
              and a.status = com.mybarber.agendamento.StatusAgendamento.AGENDADO
              and a.inicio >= :inicio and a.inicio < :fim
            """)
    List<Agendamento> buscarAgendadosDaFilialNoPeriodo(Long filialId, LocalDateTime inicio, LocalDateTime fim);
}
