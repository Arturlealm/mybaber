package com.mybarber.filial;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FilialRepository extends JpaRepository<Filial, Long> {

    List<Filial> findAllByAtivoTrueOrderByNomeAsc();

    Optional<Filial> findFirstByAtivoTrueOrderByIdAsc();

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);

    long countByAtivoTrue();
}
