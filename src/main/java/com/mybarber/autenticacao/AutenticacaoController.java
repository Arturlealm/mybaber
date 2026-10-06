package com.mybarber.autenticacao;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Autenticação")
@RestController
@RequestMapping("/api/autenticacao")
public class AutenticacaoController {

    private final AutenticacaoService autenticacaoService;

    public AutenticacaoController(AutenticacaoService autenticacaoService) {
        this.autenticacaoService = autenticacaoService;
    }

    @PostMapping("/clientes/login")
    public TokenAcessoResponse autenticarCliente(@Valid @RequestBody LoginRequest requisicao) {
        return autenticacaoService.autenticarCliente(requisicao);
    }

    @PostMapping("/funcionarios/login")
    public TokenAcessoResponse autenticarFuncionario(@Valid @RequestBody LoginRequest requisicao) {
        return autenticacaoService.autenticarFuncionario(requisicao);
    }
}
