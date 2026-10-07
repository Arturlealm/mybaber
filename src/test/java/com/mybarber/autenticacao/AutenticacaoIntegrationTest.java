package com.mybarber.autenticacao;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest(properties = {
        "mybarber.jwt.segredo=segredo-de-teste-com-pelo-menos-32-caracteres",
        "mybarber.lembrete.atraso-inicial=PT1H",
        "mybarber.administrador-inicial.nome=Administrador Teste",
        "mybarber.administrador-inicial.email=admin@teste.com",
        "mybarber.administrador-inicial.telefone=11900000000",
        "mybarber.administrador-inicial.senha=senhaAdmin123"
})
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class AutenticacaoIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void clienteDeveSeCadastrarFazerLoginEConsultarOsPropriosDados() throws Exception {
        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "João Silva", "email": "joao@teste.com", "cpf": "529.982.247-25",
                                 "telefone": "(11) 91234-5678", "senha": "senhaCliente123"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cpf").value("***.982.247-**"))
                .andExpect(jsonPath("$.senhaHash").doesNotExist());

        String token = autenticar("/api/autenticacao/clientes/login", "joao@teste.com", "senhaCliente123");

        mockMvc.perform(get("/api/clientes/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("joao@teste.com"))
                .andExpect(jsonPath("$.telefone").value("11912345678"));

        mockMvc.perform(get("/api/funcionarios").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void deveRecusarCadastroDeClienteComCpfDuplicado() throws Exception {
        String corpo = """
                {"nome": "Maria", "email": "%s", "cpf": "111.444.777-35", "telefone": "11912345678", "senha": "senhaCliente123"}
                """;

        mockMvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON).content(corpo.formatted("maria1@teste.com")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON).content(corpo.formatted("maria2@teste.com")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value("CPF já cadastrado"));
    }

    @Test
    void administradorInicialDeveCadastrarBarbeiroQueConsegueFazerLogin() throws Exception {
        String tokenAdministrador = autenticar("/api/autenticacao/funcionarios/login", "admin@teste.com", "senhaAdmin123");

        mockMvc.perform(post("/api/funcionarios")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdministrador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Carlos Barbeiro", "email": "carlos@teste.com", "telefone": "11988887777",
                                 "senha": "senhaBarbeiro123", "tipo": "BARBEIRO", "realizaAtendimentos": true}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("BARBEIRO"));

        mockMvc.perform(post("/api/autenticacao/funcionarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "carlos@teste.com", "senha": "senhaBarbeiro123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfil").value("BARBEIRO"));
    }

    @Test
    void deveRetornarNaoAutorizadoSemTokenOuComSenhaErrada() throws Exception {
        mockMvc.perform(get("/api/clientes/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        mockMvc.perform(post("/api/autenticacao/funcionarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "admin@teste.com", "senha": "senhaErrada"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("Email ou senha inválidos"));
    }

    private String autenticar(String rota, String email, String senha) throws Exception {
        String resposta = mockMvc.perform(post(rota)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"%s\", \"senha\": \"%s\"}".formatted(email, senha)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.tokenAcesso");
    }
}
