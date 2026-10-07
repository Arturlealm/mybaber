package com.mybarber.redefinicaosenha;

import static com.mybarber.compartilhado.dadospessoais.DadosPessoaisNormalizador.normalizarEmail;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mybarber.autenticacao.ControleTentativasLogin;
import com.mybarber.cliente.Cliente;
import com.mybarber.cliente.ClienteRepository;
import com.mybarber.cliente.ClienteService;
import com.mybarber.compartilhado.excecao.RegraNegocioException;
import com.mybarber.funcionario.Funcionario;
import com.mybarber.funcionario.FuncionarioRepository;
import com.mybarber.funcionario.FuncionarioService;

@Service
public class RedefinicaoSenhaService {

    private static final int TAMANHO_TOKEN_BYTES = 32;
    private static final String MENSAGEM_LINK_INVALIDO =
            "Este link de redefinição é inválido ou expirou. Solicite uma nova redefinição de senha";

    private record ContaEncontrada(Long id, String nome, String email) {
    }

    private final TokenRedefinicaoSenhaRepository tokenRedefinicaoSenhaRepository;
    private final ClienteRepository clienteRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final ClienteService clienteService;
    private final FuncionarioService funcionarioService;
    private final ControleTentativasLogin controleTentativasLogin;
    private final ApplicationEventPublisher publicadorEventos;
    private final RedefinicaoSenhaPropriedades propriedades;
    private final Clock relogio;
    private final SecureRandom geradorAleatorio = new SecureRandom();

    public RedefinicaoSenhaService(
            TokenRedefinicaoSenhaRepository tokenRedefinicaoSenhaRepository,
            ClienteRepository clienteRepository,
            FuncionarioRepository funcionarioRepository,
            ClienteService clienteService,
            FuncionarioService funcionarioService,
            ControleTentativasLogin controleTentativasLogin,
            ApplicationEventPublisher publicadorEventos,
            RedefinicaoSenhaPropriedades propriedades,
            Clock relogio) {
        this.tokenRedefinicaoSenhaRepository = tokenRedefinicaoSenhaRepository;
        this.clienteRepository = clienteRepository;
        this.funcionarioRepository = funcionarioRepository;
        this.clienteService = clienteService;
        this.funcionarioService = funcionarioService;
        this.controleTentativasLogin = controleTentativasLogin;
        this.publicadorEventos = publicadorEventos;
        this.propriedades = propriedades;
        this.relogio = relogio;
    }

    /* Não informa se o email existe, para não revelar quem tem cadastro */
    @Transactional
    public void solicitar(SolicitacaoRedefinicaoSenhaRequest requisicao) {
        Optional<ContaEncontrada> conta = buscarContaAtiva(requisicao.tipoConta(), normalizarEmail(requisicao.email()));
        if (conta.isEmpty()) {
            return;
        }

        Instant agora = relogio.instant();
        boolean solicitadoRecentemente = tokenRedefinicaoSenhaRepository.existsByTipoContaAndUsuarioIdAndCriadoEmAfter(
                requisicao.tipoConta(), conta.get().id(), agora.minus(propriedades.intervaloMinimoEntreSolicitacoes()));
        if (solicitadoRecentemente) {
            return;
        }

        tokenRedefinicaoSenhaRepository.invalidarTokensAbertos(requisicao.tipoConta(), conta.get().id(), agora);
        String token = gerarToken();
        tokenRedefinicaoSenhaRepository.save(new TokenRedefinicaoSenha(
                calcularHash(token), requisicao.tipoConta(), conta.get().id(), agora.plus(propriedades.validade())));

        publicadorEventos.publishEvent(new RedefinicaoSenhaSolicitadaEvento(
                conta.get().email(),
                conta.get().nome(),
                propriedades.urlFrontend() + "/redefinir-senha?token=" + token));
    }

    @Transactional
    public void redefinir(RedefinicaoSenhaRequest requisicao) {
        Instant agora = relogio.instant();
        TokenRedefinicaoSenha tokenRedefinicao = tokenRedefinicaoSenhaRepository
                .findByTokenHash(calcularHash(requisicao.token().strip()))
                .filter(token -> token.podeSerUsado(agora))
                .orElseThrow(() -> new RegraNegocioException(MENSAGEM_LINK_INVALIDO));

        String email = switch (tokenRedefinicao.getTipoConta()) {
            case CLIENTE -> clienteService.redefinirSenha(tokenRedefinicao.getUsuarioId(), requisicao.novaSenha()).getEmail();
            case FUNCIONARIO -> funcionarioService.redefinirSenha(tokenRedefinicao.getUsuarioId(), requisicao.novaSenha()).getEmail();
        };

        tokenRedefinicao.marcarComoUsado(agora);
        controleTentativasLogin.registrarSucesso(tokenRedefinicao.getTipoConta().name() + ":" + email);
    }

    private Optional<ContaEncontrada> buscarContaAtiva(TipoConta tipoConta, String email) {
        return switch (tipoConta) {
            case CLIENTE -> clienteRepository.findByEmail(email)
                    .filter(Cliente::isAtivo)
                    .map(cliente -> new ContaEncontrada(cliente.getId(), cliente.getNome(), cliente.getEmail()));
            case FUNCIONARIO -> funcionarioRepository.findByEmail(email)
                    .filter(Funcionario::isAtivo)
                    .map(funcionario -> new ContaEncontrada(funcionario.getId(), funcionario.getNome(), funcionario.getEmail()));
        };
    }

    private String gerarToken() {
        byte[] bytes = new byte[TAMANHO_TOKEN_BYTES];
        geradorAleatorio.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String calcularHash(String token) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException excecao) {
            throw new IllegalStateException("SHA-256 indisponível", excecao);
        }
    }
}
