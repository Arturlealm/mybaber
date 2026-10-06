package com.mybarber.filial;

import static com.mybarber.compartilhado.dadospessoais.DadosPessoaisNormalizador.normalizarNome;
import static com.mybarber.compartilhado.dadospessoais.DadosPessoaisNormalizador.normalizarTelefone;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mybarber.compartilhado.excecao.ConflitoDadosException;
import com.mybarber.compartilhado.excecao.RecursoNaoEncontradoException;
import com.mybarber.compartilhado.excecao.RegraNegocioException;

@Service
public class FilialService {

    private final FilialRepository filialRepository;

    public FilialService(FilialRepository filialRepository) {
        this.filialRepository = filialRepository;
    }

    @Transactional(readOnly = true)
    public List<Filial> listarAtivas() {
        return filialRepository.findAllByAtivoTrueOrderByNomeAsc();
    }

    @Transactional(readOnly = true)
    public Filial buscarAtivaPorId(Long id) {
        return filialRepository.findById(id)
                .filter(Filial::isAtivo)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Filial não encontrada com o id: " + id));
    }

    @Transactional(readOnly = true)
    public Filial buscarAtivaOuPadrao(Long id) {
        if (id != null) {
            return buscarAtivaPorId(id);
        }
        return filialRepository.findFirstByAtivoTrueOrderByIdAsc()
                .orElseThrow(() -> new RegraNegocioException("Nenhuma filial ativa cadastrada"));
    }

    @Transactional
    public Filial cadastrar(FilialCadastroRequest requisicao) {
        String nome = normalizarNome(requisicao.nome());
        if (filialRepository.existsByNomeIgnoreCase(nome)) {
            throw new ConflitoDadosException("Já existe uma filial com este nome");
        }

        Filial filial = new Filial();
        preencher(filial, nome, requisicao);
        return filialRepository.save(filial);
    }

    @Transactional
    public Filial atualizar(Long id, FilialCadastroRequest requisicao) {
        Filial filial = buscarAtivaPorId(id);
        String nome = normalizarNome(requisicao.nome());
        if (filialRepository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new ConflitoDadosException("Já existe uma filial com este nome");
        }

        preencher(filial, nome, requisicao);
        return filialRepository.saveAndFlush(filial);
    }

    @Transactional
    public void inativar(Long id) {
        Filial filial = buscarAtivaPorId(id);
        if (filialRepository.countByAtivoTrue() <= 1) {
            throw new RegraNegocioException("O sistema precisa ter pelo menos uma filial ativa");
        }
        filial.setAtivo(false);
    }

    private void preencher(Filial filial, String nome, FilialCadastroRequest requisicao) {
        filial.setNome(nome);
        filial.setTelefone(requisicao.telefone() == null || requisicao.telefone().isBlank()
                ? null
                : normalizarTelefone(requisicao.telefone()));
        filial.setEndereco(requisicao.endereco() == null || requisicao.endereco().isBlank()
                ? null
                : requisicao.endereco().strip());
    }
}
