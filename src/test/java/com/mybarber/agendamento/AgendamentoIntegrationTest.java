package com.mybarber.agendamento;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest(properties = {
        "mybarber.jwt.segredo=segredo-de-teste-com-pelo-menos-32-caracteres",
        "mybarber.administrador-inicial.email=admin@teste.com",
        "mybarber.administrador-inicial.telefone=11900000000",
        "mybarber.administrador-inicial.senha=senhaAdmin123"
})
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class AgendamentoIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void fluxoCompletoDeAgendamentoComRegraDeHorarioEFechamentoDeAgenda() throws Exception {
        LocalDate amanha = LocalDate.now(ZoneId.of("America/Sao_Paulo")).plusDays(1);
        String tokenAdministrador = autenticar("/api/autenticacao/funcionarios/login", "admin@teste.com", "senhaAdmin123");

        String barbeiro = executar(post("/api/funcionarios"), tokenAdministrador, """
                {"nome": "Carlos", "email": "carlos@teste.com", "telefone": "11988887777",
                 "senha": "senhaBarbeiro123", "tipo": "BARBEIRO", "realizaAtendimentos": true}
                """, 201);
        Integer idBarbeiro = JsonPath.read(barbeiro, "$.id");

        executar(put("/api/agenda/jornadas/funcionarios/" + idBarbeiro), tokenAdministrador, jornadaTodosOsDias(), 200);

        mockMvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON).content("""
                        {"nome": "João", "email": "joao@teste.com", "telefone": "11912345678", "senha": "senhaCliente123"}
                        """))
                .andExpect(status().isCreated());
        String tokenCliente = autenticar("/api/autenticacao/clientes/login", "joao@teste.com", "senhaCliente123");

        String servicos = mockMvc.perform(get("/api/servicos")).andReturn().getResponse().getContentAsString();
        List<Integer> idsCabelo = JsonPath.read(servicos, "$[?(@.nome == 'Cabelo')].id");
        List<Integer> idsBarba = JsonPath.read(servicos, "$[?(@.nome == 'Barba')].id");
        Integer idCabelo = idsCabelo.getFirst();
        Integer idBarba = idsBarba.getFirst();

        String agendamento = executar(post("/api/agendamentos"), tokenCliente, """
                {"funcionarioId": %d, "inicio": "%sT08:30:00", "servicoIds": [%d, %d]}
                """.formatted(idBarbeiro, amanha, idCabelo, idBarba), 201);
        Integer idAgendamento = JsonPath.read(agendamento, "$.id");
        assertThat((String) JsonPath.read(agendamento, "$.fim")).isEqualTo(amanha + "T09:30:00");

        String horarios = mockMvc.perform(get("/api/agendamentos/horarios-disponiveis")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenCliente)
                        .param("funcionarioId", idBarbeiro.toString())
                        .param("data", amanha.toString())
                        .param("servicoIds", idCabelo.toString()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        List<String> horariosLivres = JsonPath.read(horarios, "$.horarios");
        assertThat(horariosLivres).contains("08:00:00", "09:30:00").doesNotContain("08:30:00", "09:00:00");

        executar(post("/api/agendamentos"), tokenCliente, """
                {"funcionarioId": %d, "inicio": "%sT09:00:00", "servicoIds": [%d]}
                """.formatted(idBarbeiro, amanha, idCabelo), 422);

        String fecharDia = """
                {"data": "%s", "tipo": "FECHADO", "motivo": "Feriado"}
                """.formatted(amanha);
        executar(post("/api/agenda/ajustes"), tokenAdministrador, fecharDia, 409);

        executar(patch("/api/agendamentos/" + idAgendamento + "/cancelamento"), tokenAdministrador, "{}", 200);
        executar(post("/api/agenda/ajustes"), tokenAdministrador, fecharDia, 200);

        mockMvc.perform(get("/api/agendamentos/horarios-disponiveis")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenCliente)
                        .param("funcionarioId", idBarbeiro.toString())
                        .param("data", amanha.toString())
                        .param("servicoIds", idCabelo.toString()))
                .andExpect(jsonPath("$.horarios").isEmpty());

        executar(post("/api/agenda/ajustes"), tokenAdministrador, """
                {"data": "%s", "tipo": "ABERTO", "funcionarioId": %d, "horaInicio": "09:00", "horaFim": "12:00",
                 "motivo": "Carlos quis trabalhar no feriado"}
                """.formatted(amanha, idBarbeiro), 200);

        mockMvc.perform(get("/api/agendamentos/horarios-disponiveis")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenCliente)
                        .param("funcionarioId", idBarbeiro.toString())
                        .param("data", amanha.toString())
                        .param("servicoIds", idCabelo.toString()))
                .andExpect(jsonPath("$.horarios[0]").value("09:00:00"))
                .andExpect(jsonPath("$.horarios.length()").value(6));
    }

    private String jornadaTodosOsDias() {
        String intervalos = String.join(",", List.of("SEGUNDA", "TERCA", "QUARTA", "QUINTA", "SEXTA", "SABADO", "DOMINGO")
                .stream()
                .map(dia -> "{\"diaSemana\": \"%s\", \"horaInicio\": \"08:00\", \"horaFim\": \"18:00\"}".formatted(dia))
                .toList());
        return "{\"intervalos\": [" + intervalos + "]}";
    }

    private String executar(MockHttpServletRequestBuilder requisicao, String token, String corpo, int statusEsperado)
            throws Exception {
        return mockMvc.perform(requisicao
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().is(statusEsperado))
                .andReturn().getResponse().getContentAsString();
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
