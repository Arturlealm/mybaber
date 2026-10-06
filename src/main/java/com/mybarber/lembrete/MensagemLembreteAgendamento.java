package com.mybarber.lembrete;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import com.mybarber.agendamento.Agendamento;
import com.mybarber.agendamento.AgendamentoItem;
import com.mybarber.compartilhado.email.MensagemEmail;

@Component
public class MensagemLembreteAgendamento {

    private static final Locale PORTUGUES_BRASIL = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM", PORTUGUES_BRASIL);
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final LembreteAgendamentoPropriedades propriedades;

    public MensagemLembreteAgendamento(LembreteAgendamentoPropriedades propriedades) {
        this.propriedades = propriedades;
    }

    public MensagemEmail montar(Agendamento agendamento) {
        String nomeCliente = agendamento.getCliente().getNome();
        String data = FORMATO_DATA.format(agendamento.getInicio());
        String hora = FORMATO_HORA.format(agendamento.getInicio());
        String servicos = agendamento.getItens().stream()
                .map(AgendamentoItem::getNomeServico)
                .collect(Collectors.joining(", "));
        String barbeiro = agendamento.getFuncionario().getNome();
        String valor = formatarMoeda(agendamento.getValorTabela());
        String endereco = agendamento.getFuncionario().getFilial().getEndereco();
        String linkAgendamentos = propriedades.urlFrontend() + "/meus-agendamentos";

        String texto = """
                Olá, %s!

                Passando para lembrar do seu horário na barbearia:

                %s às %s
                %s com %s
                Valor: %s%s

                Não vai conseguir ir? Cancele pelo site para liberar o horário: %s
                """.formatted(
                nomeCliente, data, hora, servicos, barbeiro, valor,
                endereco == null ? "" : "\nEndereço: " + endereco,
                linkAgendamentos);

        String html = """
                <div style="font-family: Arial, sans-serif; max-width: 480px; color: #1f1d1a">
                  <h2 style="margin-bottom: 8px">Lembrete do seu horário</h2>
                  <p>Olá, %s!</p>
                  <div style="background: #f6ead9; border-radius: 8px; padding: 14px 16px; margin: 16px 0">
                    <p style="margin: 0; font-size: 18px; font-weight: bold">%s às %s</p>
                    <p style="margin: 6px 0 0">%s com %s</p>
                    <p style="margin: 6px 0 0">Valor: %s</p>
                    %s
                  </div>
                  <p>Não vai conseguir ir? <a href="%s" style="color: #9a6224">Cancele pelo site</a> para liberar o horário.</p>
                </div>
                """.formatted(
                HtmlUtils.htmlEscape(nomeCliente),
                data,
                hora,
                HtmlUtils.htmlEscape(servicos),
                HtmlUtils.htmlEscape(barbeiro),
                valor,
                endereco == null ? "" : "<p style=\"margin: 6px 0 0\">" + HtmlUtils.htmlEscape(endereco) + "</p>",
                linkAgendamentos);

        return new MensagemEmail(
                agendamento.getCliente().getEmail(),
                "Lembrete: seu horário " + data + " às " + hora,
                texto,
                html);
    }

    private String formatarMoeda(BigDecimal valor) {
        return NumberFormat.getCurrencyInstance(PORTUGUES_BRASIL).format(valor);
    }
}
