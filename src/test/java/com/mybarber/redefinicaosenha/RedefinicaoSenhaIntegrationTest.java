package com.mybarber.redefinicaosenha;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.mybarber.compartilhado.email.EnvioEmail;
import com.mybarber.compartilhado.email.MensagemEmail;

@SpringBootTest(properties = {
        "mybarber.jwt.segredo=segredo-de-teste-com-pelo-menos-32-caracteres",
        "mybarber.redefinicao-senha.intervalo-minimo-entre-solicitacoes=0s"
})
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class RedefinicaoSenhaIntegrationTest {

    private static final Pattern TOKEN_NO_LINK = Pattern.compile("token=([A-Za-z0-9_-]+)");

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EnvioEmail envioEmail;

    @Test
    void clienteDeveRedefinirASenhaPeloLinkRecebidoPorEmail() throws Exception {
        cadastrarCliente("ana@teste.com", "senhaAntiga123");

        solicitarRedefinicao("ana@teste.com", "CLIENTE").andExpect(status().isAccepted());

        ArgumentCaptor<MensagemEmail> emailEnviado = ArgumentCaptor.forClass(MensagemEmail.class);
        verify(envioEmail, timeout(5000)).enviar(emailEnviado.capture());
        assertThat(emailEnviado.getValue().destinatario()).isEqualTo("ana@teste.com");
        Matcher matcher = TOKEN_NO_LINK.matcher(emailEnviado.getValue().textoSimples());
        assertThat(matcher.find()).isTrue();
        String token = matcher.group(1);

        redefinir(token, "senhaNova1234").andExpect(status().isOk());

        entrar("ana@teste.com", "senhaAntiga123").andExpect(status().isUnauthorized());
        entrar("ana@teste.com", "senhaNova1234").andExpect(status().isOk());

        redefinir(token, "outraSenha123")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.mensagem").value(containsString("inválido ou expirou")));
    }

    @Test
    void emailNaoCadastradoDeveReceberAMesmaRespostaSemEnvioDeEmail() throws Exception {
        solicitarRedefinicao("ninguem@teste.com", "CLIENTE")
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.mensagem").value(
                        "Se o email estiver cadastrado, você receberá um link para criar uma nova senha"));

        verify(envioEmail, after(1000).never()).enviar(any());
    }

    @Test
    void novaSolicitacaoDeveInvalidarOLinkAnterior() throws Exception {
        cadastrarCliente("bruno@teste.com", "senhaAntiga123");

        solicitarRedefinicao("bruno@teste.com", "CLIENTE");
        solicitarRedefinicao("bruno@teste.com", "CLIENTE");

        ArgumentCaptor<MensagemEmail> emails = ArgumentCaptor.forClass(MensagemEmail.class);
        verify(envioEmail, timeout(5000).times(2)).enviar(emails.capture());
        String primeiroToken = extrairToken(emails.getAllValues().get(0));
        String segundoToken = extrairToken(emails.getAllValues().get(1));

        redefinir(primeiroToken, "senhaNova1234").andExpect(status().isUnprocessableContent());
        redefinir(segundoToken, "senhaNova1234").andExpect(status().isOk());
    }

    private String extrairToken(MensagemEmail email) {
        Matcher matcher = TOKEN_NO_LINK.matcher(email.textoSimples());
        assertThat(matcher.find()).isTrue();
        return matcher.group(1);
    }

    private void cadastrarCliente(String email, String senha) throws Exception {
        mockMvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON).content("""
                        {"nome": "Cliente", "email": "%s", "telefone": "11912345678", "senha": "%s"}
                        """.formatted(email, senha)))
                .andExpect(status().isCreated());
    }

    private ResultActions solicitarRedefinicao(String email, String tipoConta)
            throws Exception {
        return mockMvc.perform(post("/api/autenticacao/redefinicao-senha/solicitacao")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"%s\", \"tipoConta\": \"%s\"}".formatted(email, tipoConta)));
    }

    private ResultActions redefinir(String token, String novaSenha) throws Exception {
        return mockMvc.perform(post("/api/autenticacao/redefinicao-senha")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\": \"%s\", \"novaSenha\": \"%s\"}".formatted(token, novaSenha)));
    }

    private ResultActions entrar(String email, String senha) throws Exception {
        return mockMvc.perform(post("/api/autenticacao/clientes/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"%s\", \"senha\": \"%s\"}".formatted(email, senha)));
    }
}
