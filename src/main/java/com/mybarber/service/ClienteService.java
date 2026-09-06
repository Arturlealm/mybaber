package com.mybarber.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mybarber.model.Cliente;
import com.mybarber.repository.ClienteRepository;

@Service 
public class ClienteService {
    
    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository){
        this.clienteRepository = clienteRepository;
    }

    public List<Cliente> listarTodos(){
        return clienteRepository.findAll();
    }

    public Cliente salvar(Cliente cliente){
        return clienteRepository.save(cliente);
    }
}
