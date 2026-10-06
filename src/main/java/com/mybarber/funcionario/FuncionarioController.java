package com.mybarber.funcionario;

import java.net.URI;
import java.util.List;

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

@Tag(name = "Funcionários")
@RestController
@RequestMapping("/api/funcionarios")
public class FuncionarioController {

    private final FuncionarioService funcionarioService;

    public FuncionarioController(FuncionarioService funcionarioService) {
        this.funcionarioService = funcionarioService;
    }

    @GetMapping("/barbeiros")
    public List<BarbeiroResumoResponse> listarBarbeirosDisponiveis(@RequestParam(required = false) Long filialId) {
        return funcionarioService.listarBarbeirosDisponiveis(filialId).stream()
                .map(BarbeiroResumoResponse::de)
                .toList();
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('BARBEIRO', 'ADMINISTRADOR')")
    public FuncionarioResponse buscarFuncionarioAutenticado(@AuthenticationPrincipal Jwt jwt) {
        return FuncionarioResponse.de(funcionarioService.buscarAtivoPorId(idFuncionarioAutenticado(jwt)));
    }

    @PutMapping("/me/senha")
    @PreAuthorize("hasAnyRole('BARBEIRO', 'ADMINISTRADOR')")
    public ResponseEntity<Void> alterarSenhaFuncionarioAutenticado(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody FuncionarioSenhaAlteracaoRequest requisicao) {
        funcionarioService.alterarSenha(idFuncionarioAutenticado(jwt), requisicao);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public PaginaResponse<FuncionarioResponse> listarAtivos(
            @PageableDefault(size = 20, sort = "nome", direction = Sort.Direction.ASC) Pageable paginacao) {
        return PaginaResponse.de(funcionarioService.listarAtivos(paginacao), FuncionarioResponse::de);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public FuncionarioResponse buscarPorId(@PathVariable Long id) {
        return FuncionarioResponse.de(funcionarioService.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<FuncionarioResponse> cadastrar(@Valid @RequestBody FuncionarioCadastroRequest requisicao) {
        Funcionario funcionario = funcionarioService.cadastrar(requisicao);
        return ResponseEntity.created(URI.create("/api/funcionarios/" + funcionario.getId()))
                .body(FuncionarioResponse.de(funcionario));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public FuncionarioResponse atualizar(
            @PathVariable Long id,
            @Valid @RequestBody FuncionarioAtualizacaoRequest requisicao) {
        return FuncionarioResponse.de(funcionarioService.atualizar(id, requisicao));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> inativar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        funcionarioService.inativar(id, idFuncionarioAutenticado(jwt));
        return ResponseEntity.noContent().build();
    }

    private Long idFuncionarioAutenticado(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}
