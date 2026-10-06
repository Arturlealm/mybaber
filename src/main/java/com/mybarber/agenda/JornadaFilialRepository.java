package com.mybarber.agenda;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface JornadaFilialRepository extends JpaRepository<JornadaFilial, Long> {

    List<JornadaFilial> findAllByFilialId(Long filialId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from JornadaFilial j where j.filialId = :filialId")
    void excluirTodasDaFilial(Long filialId);
}
