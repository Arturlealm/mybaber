package com.mybarber.agenda;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AjusteAgendaRepository extends JpaRepository<AjusteAgenda, Long> {

    Optional<AjusteAgenda> findByFuncionarioIdAndData(Long funcionarioId, LocalDate data);

    Optional<AjusteAgenda> findByFilialIdAndDataAndFuncionarioIdIsNull(Long filialId, LocalDate data);

    List<AjusteAgenda> findAllByFilialIdAndDataBetweenOrderByDataAsc(Long filialId, LocalDate inicio, LocalDate fim);
}
