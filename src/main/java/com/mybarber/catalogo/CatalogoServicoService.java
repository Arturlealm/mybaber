package com.mybarber.catalogo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mybarber.compartilhado.excecao.ConflitoDadosException;
import com.mybarber.compartilhado.excecao.RecursoNaoEncontradoException;
import com.mybarber.compartilhado.excecao.RegraNegocioException;

@Service
public class CatalogoServicoService {

    private final ServicoOferecidoRepository servicoOferecidoRepository;
    private final CombinacaoServicoRepository combinacaoServicoRepository;

    public CatalogoServicoService(
            ServicoOferecidoRepository servicoOferecidoRepository,
            CombinacaoServicoRepository combinacaoServicoRepository) {
        this.servicoOferecidoRepository = servicoOferecidoRepository;
        this.combinacaoServicoRepository = combinacaoServicoRepository;
    }

    @Transactional(readOnly = true)
    public List<ServicoOferecido> listarServicosAtivos() {
        return servicoOferecidoRepository.findAllByAtivoTrueOrderByOrdemExibicaoAscNomeAsc();
    }

    @Transactional(readOnly = true)
    public List<CombinacaoServico> listarCombinacoesAtivas() {
        return combinacaoServicoRepository.findAllByAtivoTrueOrderByOrdemExibicaoAscNomeAsc().stream()
                .filter(combinacao -> combinacao.getServicos().stream().allMatch(ServicoOferecido::isAtivo))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ServicoOferecido> buscarServicosAtivosParaAtendimento(Collection<Long> ids) {
        Set<Long> idsUnicos = new LinkedHashSet<>(ids);
        List<ServicoOferecido> servicos = servicoOferecidoRepository.findAllByIdInAndAtivoTrue(idsUnicos);

        if (servicos.size() != idsUnicos.size()) {
            throw new RegraNegocioException("Um ou mais serviços informados não existem ou estão inativos");
        }
        if (servicos.stream().mapToInt(ServicoOferecido::getDuracaoMinutos).sum() == 0) {
            throw new RegraNegocioException("Selecione ao menos um serviço com duração");
        }

        return servicos.stream()
                .sorted((primeiro, segundo) -> Integer.compare(primeiro.getOrdemExibicao(), segundo.getOrdemExibicao()))
                .toList();
    }

    @Transactional
    public ServicoOferecido cadastrarServico(ServicoOferecidoRequest requisicao) {
        String nome = requisicao.nome().strip();
        if (servicoOferecidoRepository.existsByNomeIgnoreCase(nome)) {
            throw new ConflitoDadosException("Já existe um serviço com este nome");
        }

        ServicoOferecido servico = new ServicoOferecido();
        preencherServico(servico, nome, requisicao);
        return servicoOferecidoRepository.save(servico);
    }

    @Transactional
    public ServicoOferecido atualizarServico(Long id, ServicoOferecidoRequest requisicao) {
        ServicoOferecido servico = buscarServicoAtivo(id);
        String nome = requisicao.nome().strip();
        if (servicoOferecidoRepository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new ConflitoDadosException("Já existe um serviço com este nome");
        }

        preencherServico(servico, nome, requisicao);
        return servicoOferecidoRepository.saveAndFlush(servico);
    }

    @Transactional
    public void inativarServico(Long id) {
        buscarServicoAtivo(id).setAtivo(false);
    }

    @Transactional
    public CombinacaoServico cadastrarCombinacao(CombinacaoServicoRequest requisicao) {
        String nome = requisicao.nome().strip();
        if (combinacaoServicoRepository.existsByNomeIgnoreCase(nome)) {
            throw new ConflitoDadosException("Já existe uma combinação com este nome");
        }

        CombinacaoServico combinacao = new CombinacaoServico();
        preencherCombinacao(combinacao, nome, requisicao);
        return combinacaoServicoRepository.save(combinacao);
    }

    @Transactional
    public CombinacaoServico atualizarCombinacao(Long id, CombinacaoServicoRequest requisicao) {
        CombinacaoServico combinacao = buscarCombinacaoAtiva(id);
        String nome = requisicao.nome().strip();
        if (combinacaoServicoRepository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new ConflitoDadosException("Já existe uma combinação com este nome");
        }

        preencherCombinacao(combinacao, nome, requisicao);
        return combinacaoServicoRepository.saveAndFlush(combinacao);
    }

    @Transactional
    public void inativarCombinacao(Long id) {
        buscarCombinacaoAtiva(id).setAtivo(false);
    }

    private ServicoOferecido buscarServicoAtivo(Long id) {
        return servicoOferecidoRepository.findById(id)
                .filter(ServicoOferecido::isAtivo)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Serviço não encontrado com o id: " + id));
    }

    private CombinacaoServico buscarCombinacaoAtiva(Long id) {
        return combinacaoServicoRepository.findById(id)
                .filter(CombinacaoServico::isAtivo)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Combinação não encontrada com o id: " + id));
    }

    private void preencherServico(ServicoOferecido servico, String nome, ServicoOferecidoRequest requisicao) {
        servico.setNome(nome);
        servico.setDescricao(textoOuNulo(requisicao.descricao()));
        servico.setDuracaoMinutos(requisicao.duracaoMinutos());
        servico.setPreco(requisicao.preco());
        servico.setOrdemExibicao(requisicao.ordemExibicao() == null ? 0 : requisicao.ordemExibicao());
    }

    private void preencherCombinacao(CombinacaoServico combinacao, String nome, CombinacaoServicoRequest requisicao) {
        combinacao.setNome(nome);
        combinacao.setDescricao(textoOuNulo(requisicao.descricao()));
        combinacao.setOrdemExibicao(requisicao.ordemExibicao() == null ? 0 : requisicao.ordemExibicao());
        combinacao.setServicos(new ArrayList<>(buscarServicosAtivosParaAtendimento(requisicao.servicoIds())));
    }

    private String textoOuNulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.strip();
    }
}
