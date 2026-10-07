package com.mybarber.cliente;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest(properties = {
        "mybarber.jwt.segredo=segredo-de-teste-com-pelo-menos-32-caracteres",
        "mybarber.lembrete.atraso-inicial=PT1H",
        "mybarber.administrador-inicial.email=admin@teste.com",
        "mybarber.administrador-inicial.telefone=11900000000",
        "mybarber.administrador-inicial.senha=senhaAdmin123"
})
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class ClienteBalcaoIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void clienteCadastradoNoBalcaoDeveEntrarComASenhaPadrao() throws Exception {
        String token = autenticarAdministrador();

        cadastrarNoBalcao(token, """
                {"nome": "Pedro Primeira Vez", "telefone": "(11) 98888-1111", "email": "pedro@teste.com"}
                """)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cliente.email").value("pedro@teste.com"))
                .andExpect(jsonPath("$.cliente.telefone").value("11988881111"))
                .andExpect(jsonPath("$.senhaInicial").value("123456789"));

        entrarComoCliente("pedro@teste.com", "123456789").andExpect(status().isOk());

        cadastrarNoBalcao(token, """
                {"nome": "Outro Pedro", "telefone": "11988881111", "email": "outro@teste.com"}
                """)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value(containsString("Pedro Primeira Vez")));
    }

    @Test
    void clienteComSenhaPadraoDeveSerAvisadoNoLoginEPoderTrocarSemInformarASenhaAtual() throws Exception {
        cadastrarNoBalcao(autenticarAdministrador(), """
                {"nome": "Caio", "telefone": "11933334444", "email": "caio@teste.com"}
                """).andExpect(status().isCreated());

        String resposta = entrarComoCliente("caio@teste.com", "123456789")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usaSenhaPadrao").value(true))
                .andReturn().getResponse().getContentAsString();
        String tokenCliente = JsonPath.read(resposta, "$.tokenAcesso");

        trocarSenhaPadrao(tokenCliente, "123456789").andExpect(status().isUnprocessableContent());
        trocarSenhaPadrao(tokenCliente, "minhaSenhaNova1").andExpect(status().isNoContent());

        entrarComoCliente("caio@teste.com", "123456789").andExpect(status().isUnauthorized());
        entrarComoCliente("caio@teste.com", "minhaSenhaNova1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usaSenhaPadrao").value(false));

        trocarSenhaPadrao(tokenCliente, "outraSenha123").andExpect(status().isUnprocessableContent());
    }

    private ResultActions trocarSenhaPadrao(String token, String novaSenha) throws Exception {
        return mockMvc.perform(put("/api/clientes/me/senha-padrao")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"novaSenha\": \"%s\"}".formatted(novaSenha)));
    }

    @Test
    void emailDeveSerObrigatorioNoCadastroDoBalcao() throws Exception {
        cadastrarNoBalcao(autenticarAdministrador(), """
                {"nome": "Sem Email", "telefone": "11977771111"}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo").value("email"));
    }

    @Test
    void autocadastroComEmailDoBalcaoDeveOrientarSobreASenha() throws Exception {
        cadastrarNoBalcao(autenticarAdministrador(), """
                {"nome": "Lucas", "telefone": "11977772222", "email": "lucas@teste.com"}
                """).andExpect(status().isCreated());

        mockMvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON).content("""
                        {"nome": "Lucas", "email": "lucas@teste.com", "telefone": "11977772222", "senha": "senhaCliente123"}
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value(containsString("Esqueci minha senha")));
    }

    @Test
    void barbeiroNaoPodeCadastrarClienteNoBalcao() throws Exception {
        String tokenAdministrador = autenticarAdministrador();
        mockMvc.perform(post("/api/funcionarios")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdministrador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Beto", "email": "beto@teste.com", "telefone": "11966663333",
                                 "senha": "senhaBarbeiro123", "tipo": "BARBEIRO", "realizaAtendimentos": true}
                                """))
                .andExpect(status().isCreated());
        String tokenBarbeiro = autenticarFuncionario("beto@teste.com", "senhaBarbeiro123");

        cadastrarNoBalcao(tokenBarbeiro, """
                {"nome": "Ana", "telefone": "11955556666", "email": "ana@teste.com"}
                """).andExpect(status().isForbidden());
    }

    private ResultActions cadastrarNoBalcao(String token, String corpo) throws Exception {
        return mockMvc.perform(post("/api/clientes/balcao")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo));
    }

    private ResultActions entrarComoCliente(String email, String senha) throws Exception {
        return mockMvc.perform(post("/api/autenticacao/clientes/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"%s\", \"senha\": \"%s\"}".formatted(email, senha)));
    }

    private String autenticarAdministrador() throws Exception {
        return autenticarFuncionario("admin@teste.com", "senhaAdmin123");
    }

    private String autenticarFuncionario(String email, String senha) throws Exception {
        String resposta = mockMvc.perform(post("/api/autenticacao/funcionarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"%s\", \"senha\": \"%s\"}".formatted(email, senha)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.tokenAcesso");
    }
}
