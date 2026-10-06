package com.mybarber.agenda;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface JornadaFuncionarioRepository extends JpaRepository<JornadaFuncionario, Long> {

    List<JornadaFuncionario> findAllByFuncionarioId(Long funcionarioId);

    List<JornadaFuncionario> findAllByFuncionarioIdIn(Collection<Long> funcionarioIds);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from JornadaFuncionario j where j.funcionarioId = :funcionarioId")
    void excluirTodasDoFuncionario(Long funcionarioId);
}
