package com.mybarber.filial;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/filiais")
public class FilialController {

    private final FilialService filialService;

    public FilialController(FilialService filialService) {
        this.filialService = filialService;
    }

    @GetMapping
    public List<FilialResponse> listarAtivas() {
        return filialService.listarAtivas().stream().map(FilialResponse::de).toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<FilialResponse> cadastrar(@Valid @RequestBody FilialCadastroRequest requisicao) {
        Filial filial = filialService.cadastrar(requisicao);
        return ResponseEntity.created(URI.create("/api/filiais/" + filial.getId())).body(FilialResponse.de(filial));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public FilialResponse atualizar(@PathVariable Long id, @Valid @RequestBody FilialCadastroRequest requisicao) {
        return FilialResponse.de(filialService.atualizar(id, requisicao));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        filialService.inativar(id);
        return ResponseEntity.noContent().build();
    }
}
