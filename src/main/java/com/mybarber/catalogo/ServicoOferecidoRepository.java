package com.mybarber.catalogo;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicoOferecidoRepository extends JpaRepository<ServicoOferecido, Long> {

    List<ServicoOferecido> findAllByAtivoTrueOrderByOrdemExibicaoAscNomeAsc();

    List<ServicoOferecido> findAllByIdInAndAtivoTrue(Collection<Long> ids);

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);
}
