package com.mybarber.redefinicaosenha;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.util.HtmlUtils;

import com.mybarber.compartilhado.email.EnvioEmail;
import com.mybarber.compartilhado.email.MensagemEmail;

@Component
public class EmailRedefinicaoSenhaListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailRedefinicaoSenhaListener.class);

    private final EnvioEmail envioEmail;
    private final RedefinicaoSenhaPropriedades propriedades;

    public EmailRedefinicaoSenhaListener(EnvioEmail envioEmail, RedefinicaoSenhaPropriedades propriedades) {
        this.envioEmail = envioEmail;
        this.propriedades = propriedades;
    }

    @Async
    @TransactionalEventListener
    public void enviarEmail(RedefinicaoSenhaSolicitadaEvento evento) {
        long minutosValidade = propriedades.validade().toMinutes();
        String texto = """
                Olá, %s!

                Recebemos um pedido para redefinir a sua senha no MyBarber.
                Para criar uma nova senha, acesse o link abaixo (válido por %d minutos):

                %s

                Se você não pediu a redefinição, ignore este email. Sua senha continua a mesma.
                """.formatted(evento.nome(), minutosValidade, evento.linkRedefinicao());

        String html = """
                <div style="font-family: Arial, sans-serif; max-width: 480px; color: #1f1d1a">
                  <h2 style="margin-bottom: 8px">Redefinição de senha</h2>
                  <p>Olá, %s!</p>
                  <p>Recebemos um pedido para redefinir a sua senha no MyBarber.</p>
                  <p>
                    <a href="%s" style="display: inline-block; background: #b9782f; color: #ffffff;
                       padding: 12px 20px; border-radius: 8px; text-decoration: none; font-weight: bold">
                      Criar nova senha
                    </a>
                  </p>
                  <p style="color: #6b645a; font-size: 13px">
                    O link vale por %d minutos. Se você não pediu a redefinição, ignore este email.
                  </p>
                </div>
                """.formatted(HtmlUtils.htmlEscape(evento.nome()), evento.linkRedefinicao(), minutosValidade);

        try {
            envioEmail.enviar(new MensagemEmail(evento.email(), "Redefinição de senha - MyBarber", texto, html));
        } catch (RuntimeException excecao) {
            LOGGER.error("Falha ao enviar email de redefinição de senha para {}", evento.email(), excecao);
        }
    }
}
