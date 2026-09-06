package com.mybarber.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mybarber.model.Cliente;
import com.mybarber.service.ClienteService;

@RestController 
@RequestMapping ("api/clientes")
public class ClienteController {
    
    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService){
        this.clienteService = clienteService;
    }

    @GetMapping 
    public List<Cliente> listarTodos() {
        return clienteService.listarTodos();
    }

    @PostMapping 
    public Cliente salvar(@RequestBody Cliente cliente){
        return clienteService.salvar(cliente);
    }

    @GetMapping ("/{id}")
    public Cliente buscarPorId(@PathVariable Long id){
        return clienteService.buscarPorId(id);
    }

    @PutMapping ("/{id}")
    public Cliente atualizar(@PathVariable Long id, @RequestBody Cliente cliente){

        return clienteService.atualizar(id, cliente);
    }

    @DeleteMapping ("/{id}")
    public boolean deletar(@PathVariable Long id){

        return clienteService.deletar(id);
    }
}
