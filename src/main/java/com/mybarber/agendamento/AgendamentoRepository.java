package com.mybarber.agendamento;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

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
            order by a.inicio, a.funcionario.nome
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

    @Query("""
            select a from Agendamento a
            where a.status = com.mybarber.agendamento.StatusAgendamento.AGENDADO
              and a.lembreteEnviadoEm is null
              and a.cliente.email is not null
              and a.inicio > :agora and a.inicio <= :limite
            order by a.inicio
            """)
    List<Agendamento> buscarSemLembreteComInicioAte(LocalDateTime agora, LocalDateTime limite);

    @Query("select count(a) from Agendamento a where a.lembreteEnviadoEm >= :desde")
    long contarLembretesEnviadosDesde(Instant desde);

    /* Retorna 1 somente para quem conseguiu marcar primeiro, evitando lembrete duplicado */
    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Agendamento a set a.lembreteEnviadoEm = :agora where a.id = :id and a.lembreteEnviadoEm is null")
    int marcarLembreteEnviado(Long id, Instant agora);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Agendamento a set a.lembreteEnviadoEm = null where a.id = :id")
    void desfazerMarcacaoLembrete(Long id);
}
