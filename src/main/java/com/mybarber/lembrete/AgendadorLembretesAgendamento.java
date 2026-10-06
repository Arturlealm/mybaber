package com.mybarber.lembrete;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AgendadorLembretesAgendamento {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgendadorLembretesAgendamento.class);

    private final LembreteAgendamentoService lembreteAgendamentoService;

    public AgendadorLembretesAgendamento(LembreteAgendamentoService lembreteAgendamentoService) {
        this.lembreteAgendamentoService = lembreteAgendamentoService;
    }

    @Scheduled(
            initialDelayString = "${mybarber.lembrete.atraso-inicial:PT1M}",
            fixedDelayString = "${mybarber.lembrete.intervalo-verificacao:PT10M}")
    public void verificarLembretesPendentes() {
        int enviados = lembreteAgendamentoService.enviarLembretesPendentes();
        if (enviados > 0) {
            LOGGER.info("{} lembrete(s) de agendamento enviado(s)", enviados);
        }
    }
}
