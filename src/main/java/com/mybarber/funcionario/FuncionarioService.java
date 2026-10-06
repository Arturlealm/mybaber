package com.mybarber.funcionario;

import static com.mybarber.compartilhado.dadospessoais.DadosPessoaisNormalizador.normalizarCpf;
import static com.mybarber.compartilhado.dadospessoais.DadosPessoaisNormalizador.normalizarEmail;
import static com.mybarber.compartilhado.dadospessoais.DadosPessoaisNormalizador.normalizarNome;
import static com.mybarber.compartilhado.dadospessoais.DadosPessoaisNormalizador.normalizarTelefone;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mybarber.compartilhado.excecao.ConflitoDadosException;
import com.mybarber.compartilhado.excecao.RecursoNaoEncontradoException;
import com.mybarber.compartilhado.excecao.RegraNegocioException;

@Service
public class FuncionarioService {

    private final FuncionarioRepository funcionarioRepository;
    private final PasswordEncoder passwordEncoder;

    public FuncionarioService(FuncionarioRepository funcionarioRepository, PasswordEncoder passwordEncoder) {
        this.funcionarioRepository = funcionarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Page<Funcionario> listarAtivos(Pageable paginacao) {
        return funcionarioRepository.findAllByAtivoTrue(paginacao);
    }

    @Transactional(readOnly = true)
    public List<Funcionario> listarBarbeirosDisponiveis() {
        return funcionarioRepository.findAllByAtivoTrueAndRealizaAtendimentosTrueOrderByNomeAsc();
    }

    @Transactional(readOnly = true)
    public Funcionario buscarPorId(Long id) {
        return funcionarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Funcionário não encontrado com o id: " + id));
    }

    @Transactional(readOnly = true)
    public Funcionario buscarAtivoPorId(Long id) {
        Funcionario funcionario = buscarPorId(id);
        if (!funcionario.isAtivo()) {
            throw new RecursoNaoEncontradoException("Funcionário não encontrado com o id: " + id);
        }
        return funcionario;
    }

    @Transactional
    public Funcionario cadastrar(FuncionarioCadastroRequest requisicao) {
        String email = normalizarEmail(requisicao.email());
        String cpf = normalizarCpf(requisicao.cpf());

        if (funcionarioRepository.existsByEmail(email)) {
            throw new ConflitoDadosException("Email já cadastrado");
        }
        if (cpf != null && funcionarioRepository.existsByCpf(cpf)) {
            throw new ConflitoDadosException("CPF já cadastrado");
        }

        Funcionario funcionario = new Funcionario();
        funcionario.setNome(normalizarNome(requisicao.nome()));
        funcionario.setEmail(email);
        funcionario.setCpf(cpf);
        funcionario.setTelefone(normalizarTelefone(requisicao.telefone()));
        funcionario.setSenhaHash(passwordEncoder.encode(requisicao.senha()));
        funcionario.setTipo(requisicao.tipo());
        funcionario.setRealizaAtendimentos(requisicao.realizaAtendimentos());

        return funcionarioRepository.save(funcionario);
    }

    @Transactional
    public Funcionario atualizar(Long id, FuncionarioAtualizacaoRequest requisicao) {
        Funcionario funcionario = buscarAtivoPorId(id);
        String email = normalizarEmail(requisicao.email());
        String cpf = normalizarCpf(requisicao.cpf());

        if (funcionarioRepository.existsByEmailAndIdNot(email, id)) {
            throw new ConflitoDadosException("Email já cadastrado para outro funcionário");
        }
        if (cpf != null && funcionarioRepository.existsByCpfAndIdNot(cpf, id)) {
            throw new ConflitoDadosException("CPF já cadastrado para outro funcionário");
        }
        if (funcionario.getTipo() == TipoFuncionario.ADMINISTRADOR
                && requisicao.tipo() != TipoFuncionario.ADMINISTRADOR) {
            garantirOutroAdministradorAtivo();
        }

        funcionario.setNome(normalizarNome(requisicao.nome()));
        funcionario.setEmail(email);
        funcionario.setCpf(cpf);
        funcionario.setTelefone(normalizarTelefone(requisicao.telefone()));
        funcionario.setTipo(requisicao.tipo());
        funcionario.setRealizaAtendimentos(requisicao.realizaAtendimentos());

        return funcionarioRepository.saveAndFlush(funcionario);
    }

    @Transactional
    public void alterarSenha(Long id, FuncionarioSenhaAlteracaoRequest requisicao) {
        Funcionario funcionario = buscarAtivoPorId(id);

        if (!passwordEncoder.matches(requisicao.senhaAtual(), funcionario.getSenhaHash())) {
            throw new RegraNegocioException("A senha atual está incorreta");
        }

        funcionario.setSenhaHash(passwordEncoder.encode(requisicao.novaSenha()));
    }

    @Transactional
    public void inativar(Long id, Long idAdministradorAutenticado) {
        if (id.equals(idAdministradorAutenticado)) {
            throw new RegraNegocioException("Não é possível inativar o próprio usuário");
        }

        Funcionario funcionario = buscarAtivoPorId(id);
        if (funcionario.getTipo() == TipoFuncionario.ADMINISTRADOR) {
            garantirOutroAdministradorAtivo();
        }

        funcionario.setAtivo(false);
    }

    private void garantirOutroAdministradorAtivo() {
        if (funcionarioRepository.countByTipoAndAtivoTrue(TipoFuncionario.ADMINISTRADOR) <= 1) {
            throw new RegraNegocioException("O sistema precisa ter pelo menos um administrador ativo");
        }
    }
}
