package com.mybarber.compartilhado.email;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Component
@ConditionalOnProperty(prefix = "mybarber.email", name = "habilitado", havingValue = "true")
public class EnvioEmailSmtp implements EnvioEmail {

    private final JavaMailSender javaMailSender;
    private final EmailPropriedades emailPropriedades;

    public EnvioEmailSmtp(JavaMailSender javaMailSender, EmailPropriedades emailPropriedades) {
        this.javaMailSender = javaMailSender;
        this.emailPropriedades = emailPropriedades;
    }

    @Override
    public void enviar(MensagemEmail mensagem) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper ajudante = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());
            ajudante.setFrom(emailPropriedades.remetente(), emailPropriedades.nomeRemetente());
            ajudante.setTo(mensagem.destinatario());
            ajudante.setSubject(mensagem.assunto());
            ajudante.setText(mensagem.textoSimples(), mensagem.html());
            javaMailSender.send(mimeMessage);
        } catch (MessagingException | UnsupportedEncodingException excecao) {
            throw new IllegalStateException("Não foi possível montar o email para " + mensagem.destinatario(), excecao);
        }
    }
}
