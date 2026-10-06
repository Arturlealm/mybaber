package com.mybarber.relatorio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mybarber.agendamento.StatusAgendamento;
import com.mybarber.compartilhado.excecao.RegraNegocioException;
import com.mybarber.filial.FilialService;
import com.mybarber.relatorio.RelatorioDesempenhoBarbeirosResponse.Barbeiro;
import com.mybarber.relatorio.RelatorioDesempenhoBarbeirosResponse.Destaque;
import com.mybarber.relatorio.RelatorioDesempenhoBarbeirosResponse.Servico;
import com.mybarber.relatorio.RelatorioDesempenhoBarbeirosResponse.Totais;

@Service
public class RelatorioDesempenhoBarbeirosService {

    private static final int QUANTIDADE_MAXIMA_DIAS = 366;

    private final RelatorioAtendimentoRepository relatorioAtendimentoRepository;
    private final FilialService filialService;

    public RelatorioDesempenhoBarbeirosService(
            RelatorioAtendimentoRepository relatorioAtendimentoRepository,
            FilialService filialService) {
        this.relatorioAtendimentoRepository = relatorioAtendimentoRepository;
        this.filialService = filialService;
    }

    @Transactional(readOnly = true)
    public RelatorioDesempenhoBarbeirosResponse gerar(LocalDate inicio, LocalDate fim, Long filialId) {
        if (fim.isBefore(inicio)) {
            throw new RegraNegocioException("A data final deve ser igual ou posterior à data inicial");
        }
        if (ChronoUnit.DAYS.between(inicio, fim) >= QUANTIDADE_MAXIMA_DIAS) {
            throw new RegraNegocioException("O período do relatório deve ter no máximo " + QUANTIDADE_MAXIMA_DIAS + " dias");
        }

        Long idFilial = filialService.buscarAtivaOuPadrao(filialId).getId();
        List<ResumoStatusBarbeiro> resumos = relatorioAtendimentoRepository
                .resumirPorBarbeiroEStatus(idFilial, inicio.atStartOfDay(), fim.plusDays(1).atStartOfDay());
        List<QuantidadeServicoBarbeiro> quantidadesServicos = relatorioAtendimentoRepository
                .contarServicosConcluidosPorBarbeiro(idFilial, inicio.atStartOfDay(), fim.plusDays(1).atStartOfDay());

        Map<Long, List<ResumoStatusBarbeiro>> resumosPorBarbeiro = resumos.stream()
                .collect(Collectors.groupingBy(ResumoStatusBarbeiro::funcionarioId));
        Map<Long, List<QuantidadeServicoBarbeiro>> servicosPorBarbeiro = quantidadesServicos.stream()
                .collect(Collectors.groupingBy(QuantidadeServicoBarbeiro::funcionarioId));

        List<Barbeiro> barbeiros = resumosPorBarbeiro.values().stream()
                .map(resumosDoBarbeiro -> montarBarbeiro(
                        resumosDoBarbeiro,
                        servicosPorBarbeiro.getOrDefault(resumosDoBarbeiro.getFirst().funcionarioId(), List.of())))
                .sorted(Comparator.comparing(Barbeiro::faturamento).reversed().thenComparing(Barbeiro::nome))
                .toList();

        Totais totais = new Totais(
                barbeiros.stream().mapToLong(Barbeiro::atendimentosConcluidos).sum(),
                somar(barbeiros, Barbeiro::faturamento),
                somar(barbeiros, Barbeiro::descontos));

        return new RelatorioDesempenhoBarbeirosResponse(
                inicio,
                fim,
                idFilial,
                totais,
                destacarMaisAtendimentos(barbeiros),
                destacarMaiorFaturamento(barbeiros),
                destacarMaisAtendimentosPorServico(quantidadesServicos),
                barbeiros);
    }

    private Barbeiro montarBarbeiro(List<ResumoStatusBarbeiro> resumos, List<QuantidadeServicoBarbeiro> servicos) {
        Map<StatusAgendamento, ResumoStatusBarbeiro> porStatus = resumos.stream()
                .collect(Collectors.toMap(ResumoStatusBarbeiro::status, Function.identity()));
        ResumoStatusBarbeiro concluidos = porStatus.get(StatusAgendamento.CONCLUIDO);

        long quantidadeConcluidos = concluidos == null ? 0 : concluidos.quantidade();
        BigDecimal faturamento = concluidos == null ? BigDecimal.ZERO : concluidos.valorCobrado();
        BigDecimal descontos = concluidos == null
                ? BigDecimal.ZERO
                : concluidos.valorTabela().subtract(concluidos.valorCobrado()).max(BigDecimal.ZERO);
        BigDecimal ticketMedio = quantidadeConcluidos == 0
                ? BigDecimal.ZERO
                : faturamento.divide(BigDecimal.valueOf(quantidadeConcluidos), 2, RoundingMode.HALF_UP);

        return new Barbeiro(
                resumos.getFirst().funcionarioId(),
                resumos.getFirst().nomeFuncionario(),
                quantidadeConcluidos,
                faturamento,
                descontos,
                ticketMedio,
                quantidade(porStatus, StatusAgendamento.NAO_COMPARECEU),
                quantidade(porStatus, StatusAgendamento.CANCELADO),
                quantidade(porStatus, StatusAgendamento.AGENDADO),
                servicos.stream()
                        .map(servico -> new Servico(servico.servicoId(), servico.nomeServico(), servico.quantidade()))
                        .sorted(Comparator.comparing(Servico::quantidade).reversed())
                        .toList());
    }

    private Destaque destacarMaisAtendimentos(List<Barbeiro> barbeiros) {
        return barbeiros.stream()
                .filter(barbeiro -> barbeiro.atendimentosConcluidos() > 0)
                .max(Comparator.comparing(Barbeiro::atendimentosConcluidos))
                .map(barbeiro -> new Destaque(barbeiro.funcionarioId(), barbeiro.nome(), null, null,
                        barbeiro.atendimentosConcluidos(), barbeiro.faturamento()))
                .orElse(null);
    }

    private Destaque destacarMaiorFaturamento(List<Barbeiro> barbeiros) {
        return barbeiros.stream()
                .filter(barbeiro -> barbeiro.faturamento().signum() > 0)
                .max(Comparator.comparing(Barbeiro::faturamento))
                .map(barbeiro -> new Destaque(barbeiro.funcionarioId(), barbeiro.nome(), null, null,
                        barbeiro.atendimentosConcluidos(), barbeiro.faturamento()))
                .orElse(null);
    }

    private List<Destaque> destacarMaisAtendimentosPorServico(List<QuantidadeServicoBarbeiro> quantidades) {
        return quantidades.stream()
                .collect(Collectors.groupingBy(
                        QuantidadeServicoBarbeiro::servicoId,
                        Collectors.maxBy(Comparator.comparing(QuantidadeServicoBarbeiro::quantidade))))
                .values().stream()
                .flatMap(Optional::stream)
                .map(lider -> new Destaque(lider.funcionarioId(), lider.nomeFuncionario(), lider.servicoId(),
                        lider.nomeServico(), lider.quantidade(), null))
                .sorted(Comparator.comparing(Destaque::nomeServico))
                .toList();
    }

    private long quantidade(Map<StatusAgendamento, ResumoStatusBarbeiro> porStatus, StatusAgendamento status) {
        ResumoStatusBarbeiro resumo = porStatus.get(status);
        return resumo == null ? 0 : resumo.quantidade();
    }

    private BigDecimal somar(List<Barbeiro> barbeiros, Function<Barbeiro, BigDecimal> valor) {
        return barbeiros.stream().map(valor).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
