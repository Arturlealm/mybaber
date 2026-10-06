package com.mybarber.catalogo;

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
@RequestMapping("/api/servicos")
public class CatalogoServicoController {

    private final CatalogoServicoService catalogoServicoService;

    public CatalogoServicoController(CatalogoServicoService catalogoServicoService) {
        this.catalogoServicoService = catalogoServicoService;
    }

    @GetMapping
    public List<ServicoOferecidoResponse> listarServicosAtivos() {
        return catalogoServicoService.listarServicosAtivos().stream().map(ServicoOferecidoResponse::de).toList();
    }

    @GetMapping("/combinacoes")
    public List<CombinacaoServicoResponse> listarCombinacoesAtivas() {
        return catalogoServicoService.listarCombinacoesAtivas().stream().map(CombinacaoServicoResponse::de).toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ServicoOferecidoResponse> cadastrarServico(
            @Valid @RequestBody ServicoOferecidoRequest requisicao) {
        ServicoOferecido servico = catalogoServicoService.cadastrarServico(requisicao);
        return ResponseEntity.created(URI.create("/api/servicos/" + servico.getId()))
                .body(ServicoOferecidoResponse.de(servico));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ServicoOferecidoResponse atualizarServico(
            @PathVariable Long id,
            @Valid @RequestBody ServicoOferecidoRequest requisicao) {
        return ServicoOferecidoResponse.de(catalogoServicoService.atualizarServico(id, requisicao));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> inativarServico(@PathVariable Long id) {
        catalogoServicoService.inativarServico(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/combinacoes")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<CombinacaoServicoResponse> cadastrarCombinacao(
            @Valid @RequestBody CombinacaoServicoRequest requisicao) {
        CombinacaoServico combinacao = catalogoServicoService.cadastrarCombinacao(requisicao);
        return ResponseEntity.created(URI.create("/api/servicos/combinacoes/" + combinacao.getId()))
                .body(CombinacaoServicoResponse.de(combinacao));
    }

    @PutMapping("/combinacoes/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public CombinacaoServicoResponse atualizarCombinacao(
            @PathVariable Long id,
            @Valid @RequestBody CombinacaoServicoRequest requisicao) {
        return CombinacaoServicoResponse.de(catalogoServicoService.atualizarCombinacao(id, requisicao));
    }

    @DeleteMapping("/combinacoes/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> inativarCombinacao(@PathVariable Long id) {
        catalogoServicoService.inativarCombinacao(id);
        return ResponseEntity.noContent().build();
    }
}
