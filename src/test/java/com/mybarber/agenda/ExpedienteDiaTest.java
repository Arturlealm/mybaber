package com.mybarber.agenda;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

class ExpedienteDiaTest {

    private static final LocalDate FERIADO = LocalDate.of(2026, 11, 20);

    private final List<IntervaloHorario> jornadaSexta = List.of(
            new IntervaloHorario(LocalTime.of(13, 0), LocalTime.of(18, 0)),
            new IntervaloHorario(LocalTime.of(8, 0), LocalTime.of(12, 0)));

    @Test
    void deveUsarJornadaQuandoNaoHaAjustes() {
        ExpedienteDia expediente = ExpedienteDia.calcular(jornadaSexta, OrigemExpediente.JORNADA_FILIAL, null, null);

        assertThat(expediente.situacao()).isEqualTo(SituacaoExpediente.ABERTO);
        assertThat(expediente.origem()).isEqualTo(OrigemExpediente.JORNADA_FILIAL);
        assertThat(expediente.intervalos()).containsExactly(
                new IntervaloHorario(LocalTime.of(8, 0), LocalTime.of(12, 0)),
                new IntervaloHorario(LocalTime.of(13, 0), LocalTime.of(18, 0)));
    }

    @Test
    void deveFicarFechadoQuandoNaoHaJornadaNoDia() {
        ExpedienteDia expediente = ExpedienteDia.calcular(List.of(), OrigemExpediente.JORNADA_FUNCIONARIO, null, null);

        assertThat(expediente.situacao()).isEqualTo(SituacaoExpediente.FECHADO);
    }

    @Test
    void feriadoFechadoParaTodosDeveFecharAAgendaDoBarbeiro() {
        ExpedienteDia expediente = ExpedienteDia.calcular(
                jornadaSexta, OrigemExpediente.JORNADA_FILIAL, null, ajusteGeralFechado());

        assertThat(expediente.situacao()).isEqualTo(SituacaoExpediente.FECHADO);
        assertThat(expediente.origem()).isEqualTo(OrigemExpediente.AJUSTE_GERAL);
        assertThat(expediente.motivo()).isEqualTo("Feriado");
    }

    @Test
    void barbeiroQueQuerTrabalharNoFeriadoDeveTerAgendaAberta() {
        AjusteAgenda abertoParaBarbeiro = new AjusteAgenda();
        abertoParaBarbeiro.setFuncionarioId(1L);
        abertoParaBarbeiro.setData(FERIADO);
        abertoParaBarbeiro.setTipo(TipoAjusteAgenda.ABERTO);
        abertoParaBarbeiro.setHoraInicio(LocalTime.of(9, 0));
        abertoParaBarbeiro.setHoraFim(LocalTime.of(13, 0));

        ExpedienteDia expediente = ExpedienteDia.calcular(
                jornadaSexta, OrigemExpediente.JORNADA_FILIAL, abertoParaBarbeiro, ajusteGeralFechado());

        assertThat(expediente.situacao()).isEqualTo(SituacaoExpediente.ABERTO);
        assertThat(expediente.origem()).isEqualTo(OrigemExpediente.AJUSTE_FUNCIONARIO);
        assertThat(expediente.intervalos()).containsExactly(new IntervaloHorario(LocalTime.of(9, 0), LocalTime.of(13, 0)));
    }

    private AjusteAgenda ajusteGeralFechado() {
        AjusteAgenda ajuste = new AjusteAgenda();
        ajuste.setData(FERIADO);
        ajuste.setTipo(TipoAjusteAgenda.FECHADO);
        ajuste.setMotivo("Feriado");
        return ajuste;
    }
}
