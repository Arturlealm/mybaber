package com.mybarber.funcionario;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {

    Optional<Funcionario> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByCpf(String cpf);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByCpfAndIdNot(String cpf, Long id);

    boolean existsByTipoAndAtivoTrue(TipoFuncionario tipo);

    long countByTipoAndAtivoTrue(TipoFuncionario tipo);

    Page<Funcionario> findAllByAtivoTrue(Pageable paginacao);

    List<Funcionario> findAllByAtivoTrueAndRealizaAtendimentosTrueOrderByNomeAsc();

    List<Funcionario> findAllByAtivoTrueAndRealizaAtendimentosTrueAndFilialIdOrderByNomeAsc(Long filialId);
}
