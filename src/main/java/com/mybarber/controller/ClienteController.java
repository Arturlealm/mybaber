package com.mybarber.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import jakarta.validation.Valid;

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
    public ResponseEntity<Cliente> salvar(@Valid @RequestBody Cliente cliente){
        
        Cliente clienteSalvo = clienteService.salvar(cliente);
            
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteSalvo);

    }

    @GetMapping ("/{id}")
    public Cliente buscarPorId(@PathVariable Long id){
        return clienteService.buscarPorId(id);
    }

    @PutMapping ("/{id}")
    public Cliente atualizar(@Valid @PathVariable Long id, @RequestBody Cliente cliente){

        return clienteService.atualizar(id, cliente);
    }

    @DeleteMapping ("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id){

        clienteService.deletar(id);

        return ResponseEntity.noContent().build();
    }
}
