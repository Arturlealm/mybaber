package com.mybarber.cliente;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByCpf(String cpf);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByCpfAndIdNot(String cpf, Long id);

    Page<Cliente> findAllByAtivoTrue(Pageable paginacao);

    Page<Cliente> findAllByAtivoTrueAndNomeContainingIgnoreCase(String nome, Pageable paginacao);
}
