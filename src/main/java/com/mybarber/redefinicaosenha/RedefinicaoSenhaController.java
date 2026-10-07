package com.mybarber.redefinicaosenha;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Autenticação")
@RestController
@RequestMapping("/api/autenticacao/redefinicao-senha")
public class RedefinicaoSenhaController {

    public record MensagemRedefinicaoSenhaResponse(String mensagem) {
    }

    private final RedefinicaoSenhaService redefinicaoSenhaService;

    public RedefinicaoSenhaController(RedefinicaoSenhaService redefinicaoSenhaService) {
        this.redefinicaoSenhaService = redefinicaoSenhaService;
    }

    @PostMapping("/solicitacao")
    public ResponseEntity<MensagemRedefinicaoSenhaResponse> solicitar(
            @Valid @RequestBody SolicitacaoRedefinicaoSenhaRequest requisicao) {
        redefinicaoSenhaService.solicitar(requisicao);
        return ResponseEntity.accepted().body(new MensagemRedefinicaoSenhaResponse(
                "Se o email estiver cadastrado, você receberá um link para criar uma nova senha"));
    }

    @PostMapping
    public MensagemRedefinicaoSenhaResponse redefinir(@Valid @RequestBody RedefinicaoSenhaRequest requisicao) {
        redefinicaoSenhaService.redefinir(requisicao);
        return new MensagemRedefinicaoSenhaResponse("Senha alterada. Você já pode entrar com a nova senha");
    }
}
