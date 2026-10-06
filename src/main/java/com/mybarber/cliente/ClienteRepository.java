package com.mybarber.cliente;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    boolean existsByEmail(String email);

    boolean existsByCpf(String cpf);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByCpfAndIdNot(String cpf, Long id);

    Page<Cliente> findAllByAtivoTrue(Pageable paginacao);

    Page<Cliente> findAllByAtivoTrueAndNomeContainingIgnoreCase(String nome, Pageable paginacao);
}
