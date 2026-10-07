package com.mybarber.lembrete;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.mybarber.agendamento.Agendamento;
import com.mybarber.agendamento.AgendamentoRepository;
import com.mybarber.compartilhado.email.EnvioEmail;

@Service
public class LembreteAgendamentoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LembreteAgendamentoService.class);

    private final AgendamentoRepository agendamentoRepository;
    private final MensagemLembreteAgendamento mensagemLembreteAgendamento;
    private final EnvioEmail envioEmail;
    private final LembreteAgendamentoPropriedades propriedades;
    private final Clock relogio;

    public LembreteAgendamentoService(
            AgendamentoRepository agendamentoRepository,
            MensagemLembreteAgendamento mensagemLembreteAgendamento,
            EnvioEmail envioEmail,
            LembreteAgendamentoPropriedades propriedades,
            Clock relogio) {
        this.agendamentoRepository = agendamentoRepository;
        this.mensagemLembreteAgendamento = mensagemLembreteAgendamento;
        this.envioEmail = envioEmail;
        this.propriedades = propriedades;
        this.relogio = relogio;
    }

    public int enviarLembretesPendentes() {
        if (!propriedades.habilitado()) {
            return 0;
        }

        Instant inicioDoDia = LocalDate.now(relogio).atStartOfDay(relogio.getZone()).toInstant();
        long disponiveisHoje = propriedades.limiteDiario() - agendamentoRepository.contarLembretesEnviadosDesde(inicioDoDia);
        if (disponiveisHoje <= 0) {
            LOGGER.warn("Limite diário de {} lembretes atingido; os pendentes serão enviados quando houver saldo",
                    propriedades.limiteDiario());
            return 0;
        }

        LocalDateTime agora = LocalDateTime.now(relogio);
        List<Agendamento> pendentes = agendamentoRepository
                .buscarSemLembreteComInicioAte(agora, agora.plus(propriedades.antecedencia()))
                .stream()
                .filter(this::foiAgendadoComAntecedencia)
                .limit(disponiveisHoje)
                .toList();

        int enviados = 0;
        for (Agendamento agendamento : pendentes) {
            if (enviar(agendamento)) {
                enviados++;
            }
        }
        return enviados;
    }

    /* Quem agendou em cima da hora já sabe do horário; o lembrete seria só ruído */
    private boolean foiAgendadoComAntecedencia(Agendamento agendamento) {
        LocalDateTime criadoEm = LocalDateTime.ofInstant(agendamento.getCriadoEm(), relogio.getZone());
        return Duration.between(criadoEm, agendamento.getInicio())
                .compareTo(propriedades.antecedenciaMinimaDesdeCriacao()) >= 0;
    }

    private boolean enviar(Agendamento agendamento) {
        if (agendamentoRepository.marcarLembreteEnviado(agendamento.getId(), relogio.instant()) == 0) {
            return false;
        }
        try {
            envioEmail.enviar(mensagemLembreteAgendamento.montar(agendamento));
            return true;
        } catch (RuntimeException excecao) {
            agendamentoRepository.desfazerMarcacaoLembrete(agendamento.getId());
            LOGGER.error("Falha ao enviar lembrete do agendamento {}", agendamento.getId(), excecao);
            return false;
        }
    }
}
