package com.mybarber.agendamento;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.mybarber.agenda.IntervaloHorario;
import com.mybarber.agendamento.CalculadoraHorariosDisponiveis.PeriodoOcupado;

class CalculadoraHorariosDisponiveisTest {

    private static final LocalDate DATA = LocalDate.of(2026, 10, 7);
    private static final List<IntervaloHorario> MANHA = List.of(new IntervaloHorario(LocalTime.of(8, 0), LocalTime.of(10, 0)));

    @Test
    void deveOferecerHorariosDeTrintaEmTrintaMinutosQuandoAAgendaEstaLivre() {
        List<LocalTime> horarios = calcular(30, List.of());

        assertThat(horarios).containsExactly(hora(8, 0), hora(8, 30), hora(9, 0), hora(9, 30));
    }

    @Test
    void cabeloEBarbaAsOitoEMeiaDeveLiberarOProximoHorarioSomenteAsNoveEMeia() {
        List<PeriodoOcupado> ocupados = List.of(new PeriodoOcupado(DATA.atTime(8, 30), DATA.atTime(9, 30)));

        List<LocalTime> horarios = calcular(30, ocupados);

        assertThat(horarios).containsExactly(hora(8, 0), hora(9, 30));
    }

    @Test
    void servicoDeUmaHoraNaoDeveSerOferecidoQuandoNaoCabeAntesDoProximoAgendamento() {
        List<PeriodoOcupado> ocupados = List.of(new PeriodoOcupado(DATA.atTime(8, 30), DATA.atTime(9, 0)));

        List<LocalTime> horarios = calcular(60, ocupados);

        assertThat(horarios).containsExactly(hora(9, 0));
    }

    @Test
    void naoDeveOferecerHorarioQueTerminaDepoisDoFimDoExpediente() {
        List<LocalTime> horarios = calcular(60, List.of());

        assertThat(horarios).containsExactly(hora(8, 0), hora(8, 30), hora(9, 0));
    }

    @Test
    void naoDeveOferecerHorariosAnterioresAoInicioMinimoPermitido() {
        List<LocalTime> horarios = CalculadoraHorariosDisponiveis.calcular(
                DATA, MANHA, 30, 30, List.of(), DATA.atTime(8, 45));

        assertThat(horarios).containsExactly(hora(9, 0), hora(9, 30));
    }

    @Test
    void deveRespeitarPausaEntreIntervalosDoExpediente() {
        List<IntervaloHorario> comAlmoco = List.of(
                new IntervaloHorario(LocalTime.of(11, 0), LocalTime.of(12, 0)),
                new IntervaloHorario(LocalTime.of(13, 0), LocalTime.of(14, 0)));

        List<LocalTime> horarios = CalculadoraHorariosDisponiveis.calcular(
                DATA, comAlmoco, 60, 30, List.of(), DATA.atStartOfDay());

        assertThat(horarios).containsExactly(hora(11, 0), hora(13, 0));
    }

    private List<LocalTime> calcular(int duracaoMinutos, List<PeriodoOcupado> ocupados) {
        return CalculadoraHorariosDisponiveis.calcular(DATA, MANHA, duracaoMinutos, 30, ocupados, DATA.atStartOfDay());
    }

    private LocalTime hora(int hora, int minuto) {
        return LocalTime.of(hora, minuto);
    }
}
