package com.mybarber.compartilhado.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/* Usado em desenvolvimento, quando o SMTP não está configurado: o conteúdo do email aparece no log */
@Component
@ConditionalOnProperty(prefix = "mybarber.email", name = "habilitado", havingValue = "false", matchIfMissing = true)
public class EnvioEmailRegistroLog implements EnvioEmail {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnvioEmailRegistroLog.class);

    @Override
    public void enviar(MensagemEmail mensagem) {
        LOGGER.info("Email não enviado (mybarber.email.habilitado=false). Para: {} | Assunto: {}\n{}",
                mensagem.destinatario(), mensagem.assunto(), mensagem.textoSimples());
    }
}
