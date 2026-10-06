package com.mybarber.autenticacao;

import static com.mybarber.compartilhado.dadospessoais.DadosPessoaisNormalizador.normalizarEmail;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mybarber.cliente.Cliente;
import com.mybarber.cliente.ClienteRepository;
import com.mybarber.funcionario.Funcionario;
import com.mybarber.funcionario.FuncionarioRepository;

@Service
public class AutenticacaoService {

    private final ClienteRepository clienteRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenAcessoService tokenAcessoService;
    private final String senhaHashFicticia;

    public AutenticacaoService(
            ClienteRepository clienteRepository,
            FuncionarioRepository funcionarioRepository,
            PasswordEncoder passwordEncoder,
            TokenAcessoService tokenAcessoService) {
        this.clienteRepository = clienteRepository;
        this.funcionarioRepository = funcionarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenAcessoService = tokenAcessoService;
        this.senhaHashFicticia = passwordEncoder.encode("senha-ficticia-tempo-constante");
    }

    @Transactional(readOnly = true)
    public TokenAcessoResponse autenticarCliente(LoginRequest requisicao) {
        Cliente cliente = clienteRepository.findByEmail(normalizarEmail(requisicao.email()))
                .filter(Cliente::isAtivo)
                .orElse(null);

        if (!senhaConfere(requisicao.senha(), cliente == null ? null : cliente.getSenhaHash())) {
            throw new CredenciaisInvalidasException();
        }

        return tokenAcessoService.gerar(cliente.getId(), cliente.getNome(), PerfilAcesso.CLIENTE);
    }

    @Transactional(readOnly = true)
    public TokenAcessoResponse autenticarFuncionario(LoginRequest requisicao) {
        Funcionario funcionario = funcionarioRepository.findByEmail(normalizarEmail(requisicao.email()))
                .filter(Funcionario::isAtivo)
                .orElse(null);

        if (!senhaConfere(requisicao.senha(), funcionario == null ? null : funcionario.getSenhaHash())) {
            throw new CredenciaisInvalidasException();
        }

        return tokenAcessoService.gerar(
                funcionario.getId(), funcionario.getNome(), funcionario.getTipo().getPerfilAcesso());
    }

    /* Compara com um hash fictício quando o usuário não existe para não revelar emails cadastrados pelo tempo de resposta */
    private boolean senhaConfere(String senhaInformada, String senhaHash) {
        boolean confere = passwordEncoder.matches(senhaInformada, senhaHash == null ? senhaHashFicticia : senhaHash);
        return senhaHash != null && confere;
    }
}
