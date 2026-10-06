package com.mybarber.agenda;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mybarber.filial.FilialService;
import com.mybarber.funcionario.FuncionarioService;

@Service
public class JornadaFuncionarioService {

    private final JornadaFuncionarioRepository jornadaFuncionarioRepository;
    private final JornadaFilialRepository jornadaFilialRepository;
    private final FuncionarioService funcionarioService;
    private final FilialService filialService;

    public JornadaFuncionarioService(
            JornadaFuncionarioRepository jornadaFuncionarioRepository,
            JornadaFilialRepository jornadaFilialRepository,
            FuncionarioService funcionarioService,
            FilialService filialService) {
        this.jornadaFuncionarioRepository = jornadaFuncionarioRepository;
        this.jornadaFilialRepository = jornadaFilialRepository;
        this.funcionarioService = funcionarioService;
        this.filialService = filialService;
    }

    @Transactional(readOnly = true)
    public List<JornadaFuncionario> buscarJornada(Long funcionarioId) {
        funcionarioService.buscarAtivoPorId(funcionarioId);
        return jornadaFuncionarioRepository.findAllByFuncionarioId(funcionarioId);
    }

    /* Lista vazia remove a jornada própria e o barbeiro volta a seguir a jornada padrão da filial */
    @Transactional
    public List<JornadaFuncionario> definirJornada(Long funcionarioId, JornadaSemanalRequest requisicao) {
        funcionarioService.buscarBarbeiroAtivoPorId(funcionarioId);
        ValidadorIntervalosJornada.validar(requisicao.intervalos());

        jornadaFuncionarioRepository.excluirTodasDoFuncionario(funcionarioId);
        List<JornadaFuncionario> jornadas = requisicao.intervalos().stream()
                .map(intervalo -> new JornadaFuncionario(
                        funcionarioId, intervalo.diaSemana(), intervalo.horaInicio(), intervalo.horaFim()))
                .toList();
        return jornadaFuncionarioRepository.saveAll(jornadas);
    }

    @Transactional(readOnly = true)
    public List<JornadaFilial> buscarJornadaDaFilial(Long filialId) {
        filialService.buscarAtivaPorId(filialId);
        return jornadaFilialRepository.findAllByFilialId(filialId);
    }

    @Transactional
    public List<JornadaFilial> definirJornadaDaFilial(Long filialId, JornadaSemanalRequest requisicao) {
        filialService.buscarAtivaPorId(filialId);
        ValidadorIntervalosJornada.validar(requisicao.intervalos());

        jornadaFilialRepository.excluirTodasDaFilial(filialId);
        List<JornadaFilial> jornadas = requisicao.intervalos().stream()
                .map(intervalo -> new JornadaFilial(
                        filialId, intervalo.diaSemana(), intervalo.horaInicio(), intervalo.horaFim()))
                .toList();
        return jornadaFilialRepository.saveAll(jornadas);
    }
}
