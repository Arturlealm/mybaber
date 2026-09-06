package com.mybarber.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mybarber.model.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    boolean existsByEmail(String email);
    boolean existsByCpf(String cpf);

}
