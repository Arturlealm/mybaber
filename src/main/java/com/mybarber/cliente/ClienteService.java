package com.mybarber.cliente;

import static com.mybarber.compartilhado.dadospessoais.DadosPessoaisNormalizador.normalizarCpf;
import static com.mybarber.compartilhado.dadospessoais.DadosPessoaisNormalizador.normalizarEmail;
import static com.mybarber.compartilhado.dadospessoais.DadosPessoaisNormalizador.normalizarNome;
import static com.mybarber.compartilhado.dadospessoais.DadosPessoaisNormalizador.normalizarTelefone;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mybarber.compartilhado.excecao.ConflitoDadosException;
import com.mybarber.compartilhado.excecao.RecursoNaoEncontradoException;
import com.mybarber.compartilhado.excecao.RegraNegocioException;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;

    public ClienteService(ClienteRepository clienteRepository, PasswordEncoder passwordEncoder) {
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Page<Cliente> listarAtivos(String nome, Pageable paginacao) {
        if (nome == null || nome.isBlank()) {
            return clienteRepository.findAllByAtivoTrue(paginacao);
        }
        return clienteRepository.findAllByAtivoTrueAndNomeContainingIgnoreCase(nome.strip(), paginacao);
    }

    @Transactional(readOnly = true)
    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado com o id: " + id));
    }

    @Transactional(readOnly = true)
    public Cliente buscarAtivoPorId(Long id) {
        Cliente cliente = buscarPorId(id);
        if (!cliente.isAtivo()) {
            throw new RecursoNaoEncontradoException("Cliente não encontrado com o id: " + id);
        }
        return cliente;
    }

    @Transactional
    public Cliente cadastrar(ClienteCadastroRequest requisicao) {
        String email = normalizarEmail(requisicao.email());
        String cpf = normalizarCpf(requisicao.cpf());

        if (clienteRepository.existsByEmail(email)) {
            throw new ConflitoDadosException("Email já cadastrado");
        }
        if (cpf != null && clienteRepository.existsByCpf(cpf)) {
            throw new ConflitoDadosException("CPF já cadastrado");
        }

        Cliente cliente = new Cliente();
        cliente.setNome(normalizarNome(requisicao.nome()));
        cliente.setEmail(email);
        cliente.setCpf(cpf);
        cliente.setTelefone(normalizarTelefone(requisicao.telefone()));
        cliente.setSenhaHash(passwordEncoder.encode(requisicao.senha()));

        return clienteRepository.save(cliente);
    }

    @Transactional
    public Cliente atualizar(Long id, ClienteAtualizacaoRequest requisicao) {
        Cliente cliente = buscarPorId(id);
        String email = normalizarEmail(requisicao.email());
        String cpf = normalizarCpf(requisicao.cpf());

        if (clienteRepository.existsByEmailAndIdNot(email, id)) {
            throw new ConflitoDadosException("Email já cadastrado para outro cliente");
        }
        if (cpf != null && clienteRepository.existsByCpfAndIdNot(cpf, id)) {
            throw new ConflitoDadosException("CPF já cadastrado para outro cliente");
        }

        cliente.setNome(normalizarNome(requisicao.nome()));
        cliente.setEmail(email);
        cliente.setCpf(cpf);
        cliente.setTelefone(normalizarTelefone(requisicao.telefone()));

        return clienteRepository.saveAndFlush(cliente);
    }

    @Transactional
    public void alterarSenha(Long id, ClienteSenhaAlteracaoRequest requisicao) {
        Cliente cliente = buscarAtivoPorId(id);

        if (!passwordEncoder.matches(requisicao.senhaAtual(), cliente.getSenhaHash())) {
            throw new RegraNegocioException("A senha atual está incorreta");
        }

        cliente.setSenhaHash(passwordEncoder.encode(requisicao.novaSenha()));
    }

    @Transactional
    public void inativar(Long id) {
        buscarPorId(id).setAtivo(false);
    }
}
