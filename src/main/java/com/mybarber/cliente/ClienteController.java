package com.mybarber.cliente;

import java.net.URI;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mybarber.compartilhado.paginacao.PaginaResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Clientes")
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> cadastrar(@Valid @RequestBody ClienteCadastroRequest requisicao) {
        Cliente cliente = clienteService.cadastrar(requisicao);
        return ResponseEntity.created(URI.create("/api/clientes/" + cliente.getId()))
                .body(ClienteResponse.de(cliente));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CLIENTE')")
    public ClienteResponse buscarClienteAutenticado(@AuthenticationPrincipal Jwt jwt) {
        return ClienteResponse.de(clienteService.buscarAtivoPorId(idClienteAutenticado(jwt)));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('CLIENTE')")
    public ClienteResponse atualizarClienteAutenticado(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ClienteAtualizacaoRequest requisicao) {
        Long id = clienteService.buscarAtivoPorId(idClienteAutenticado(jwt)).getId();
        return ClienteResponse.de(clienteService.atualizar(id, requisicao));
    }

    @PutMapping("/me/senha")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<Void> alterarSenhaClienteAutenticado(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ClienteSenhaAlteracaoRequest requisicao) {
        clienteService.alterarSenha(idClienteAutenticado(jwt), requisicao);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('BARBEIRO', 'ADMINISTRADOR')")
    public PaginaResponse<ClienteResponse> listarAtivos(
            @RequestParam(required = false) String busca,
            @PageableDefault(size = 20, sort = "nome", direction = Sort.Direction.ASC) Pageable paginacao) {
        return PaginaResponse.de(clienteService.listarAtivos(busca, paginacao), ClienteResponse::de);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('BARBEIRO', 'ADMINISTRADOR')")
    public ClienteResponse buscarPorId(@PathVariable Long id) {
        return ClienteResponse.de(clienteService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ClienteResponse atualizar(@PathVariable Long id, @Valid @RequestBody ClienteAtualizacaoRequest requisicao) {
        return ClienteResponse.de(clienteService.atualizar(id, requisicao));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        clienteService.inativar(id);
        return ResponseEntity.noContent().build();
    }

    private Long idClienteAutenticado(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}
