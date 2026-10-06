package com.mybarber.catalogo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CombinacaoServicoRepository extends JpaRepository<CombinacaoServico, Long> {

    List<CombinacaoServico> findAllByAtivoTrueOrderByOrdemExibicaoAscNomeAsc();

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);
}
