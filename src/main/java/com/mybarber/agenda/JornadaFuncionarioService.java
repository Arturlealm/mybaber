package com.mybarber.agenda;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mybarber.compartilhado.excecao.RegraNegocioException;
import com.mybarber.funcionario.FuncionarioService;

@Service
public class JornadaFuncionarioService {

    private final JornadaFuncionarioRepository jornadaFuncionarioRepository;
    private final FuncionarioService funcionarioService;

    public JornadaFuncionarioService(
            JornadaFuncionarioRepository jornadaFuncionarioRepository,
            FuncionarioService funcionarioService) {
        this.jornadaFuncionarioRepository = jornadaFuncionarioRepository;
        this.funcionarioService = funcionarioService;
    }

    @Transactional(readOnly = true)
    public List<JornadaFuncionario> buscarJornada(Long funcionarioId) {
        funcionarioService.buscarAtivoPorId(funcionarioId);
        return jornadaFuncionarioRepository.findAllByFuncionarioId(funcionarioId);
    }

    @Transactional
    public List<JornadaFuncionario> definirJornada(Long funcionarioId, JornadaFuncionarioRequest requisicao) {
        funcionarioService.buscarBarbeiroAtivoPorId(funcionarioId);
        validarIntervalos(requisicao.intervalos());

        jornadaFuncionarioRepository.excluirTodasDoFuncionario(funcionarioId);
        List<JornadaFuncionario> jornadas = requisicao.intervalos().stream()
                .map(intervalo -> new JornadaFuncionario(
                        funcionarioId, intervalo.diaSemana(), intervalo.horaInicio(), intervalo.horaFim()))
                .toList();
        return jornadaFuncionarioRepository.saveAll(jornadas);
    }

    private void validarIntervalos(List<JornadaFuncionarioIntervaloRequest> intervalos) {
        for (JornadaFuncionarioIntervaloRequest intervalo : intervalos) {
            if (!intervalo.horaFim().isAfter(intervalo.horaInicio())) {
                throw new RegraNegocioException("A hora de fim deve ser posterior à hora de início em "
                        + intervalo.diaSemana());
            }
        }

        Map<DiaSemana, List<JornadaFuncionarioIntervaloRequest>> intervalosPorDia = intervalos.stream()
                .collect(Collectors.groupingBy(JornadaFuncionarioIntervaloRequest::diaSemana));

        intervalosPorDia.forEach((dia, intervalosDoDia) -> {
            List<JornadaFuncionarioIntervaloRequest> ordenados = intervalosDoDia.stream()
                    .sorted(Comparator.comparing(JornadaFuncionarioIntervaloRequest::horaInicio))
                    .toList();
            for (int i = 1; i < ordenados.size(); i++) {
                if (ordenados.get(i).horaInicio().isBefore(ordenados.get(i - 1).horaFim())) {
                    throw new RegraNegocioException("Existem intervalos sobrepostos em " + dia);
                }
            }
        });
    }
}
