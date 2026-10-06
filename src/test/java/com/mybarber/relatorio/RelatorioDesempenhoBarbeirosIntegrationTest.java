package com.mybarber.relatorio;

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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
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
class RelatorioDesempenhoBarbeirosIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void deveApontarQuemFezMaisCabeloMaisBarbaEQuemFaturouMais() throws Exception {
        long ana = inserirBarbeiro("Ana", "ana@teste.com");
        long bruno = inserirBarbeiro("Bruno", "bruno@teste.com");
        long cliente = jdbcTemplate.queryForObject("""
                INSERT INTO clientes (nome, email, telefone, senha_hash) VALUES ('Cliente', 'cliente@teste.com', '11911111111', 'x')
                RETURNING id
                """, Long.class);

        inserirAtendimento(cliente, ana, "2026-03-02T08:00", "CONCLUIDO", "40.00", "Cabelo");
        inserirAtendimento(cliente, ana, "2026-03-02T09:00", "CONCLUIDO", "40.00", "Cabelo");
        inserirAtendimento(cliente, bruno, "2026-03-02T08:00", "CONCLUIDO", "60.00", "Cabelo", "Barba", "Sobrancelha");
        inserirAtendimento(cliente, bruno, "2026-03-03T08:00", "CONCLUIDO", "30.00", "Barba");
        inserirAtendimento(cliente, bruno, "2026-03-04T08:00", "NAO_COMPARECEU", null, "Barba");

        String token = autenticarAdministrador();

        mockMvc.perform(get("/api/relatorios/desempenho-barbeiros")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .param("inicio", "2026-03-01")
                        .param("fim", "2026-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totais.atendimentosConcluidos").value(4))
                .andExpect(jsonPath("$.totais.faturamento").value(170.00))
                .andExpect(jsonPath("$.totais.descontos").value(10.00))
                .andExpect(jsonPath("$.maiorFaturamento.nomeFuncionario").value("Bruno"))
                .andExpect(jsonPath("$.maisAtendimentosPorServico[?(@.nomeServico == 'Cabelo')].nomeFuncionario").value("Ana"))
                .andExpect(jsonPath("$.maisAtendimentosPorServico[?(@.nomeServico == 'Barba')].nomeFuncionario").value("Bruno"))
                .andExpect(jsonPath("$.barbeiros[?(@.nome == 'Bruno')].naoComparecimentos").value(1));
    }

    private long inserirBarbeiro(String nome, String email) {
        return jdbcTemplate.queryForObject("""
                INSERT INTO funcionarios (nome, email, telefone, senha_hash, tipo, realiza_atendimentos, filial_id)
                VALUES (?, ?, '11922222222', 'x', 'BARBEIRO', TRUE, (SELECT id FROM filiais WHERE nome = 'Matriz'))
                RETURNING id
                """, Long.class, nome, email);
    }

    private void inserirAtendimento(
            long cliente, long barbeiro, String inicio, String status, String valorCobrado, String... servicos) {
        long agendamento = jdbcTemplate.queryForObject("""
                INSERT INTO agendamentos (cliente_id, funcionario_id, filial_id, inicio, fim, status, valor_tabela, valor_cobrado)
                SELECT ?, ?, (SELECT id FROM filiais WHERE nome = 'Matriz'), CAST(? AS TIMESTAMP),
                       CAST(? AS TIMESTAMP) + make_interval(mins => SUM(s.duracao_minutos)::int), ?, SUM(s.preco), CAST(? AS NUMERIC)
                FROM servicos s WHERE s.nome = ANY (?)
                RETURNING id
                """, Long.class, cliente, barbeiro, inicio, inicio, status, valorCobrado, servicos);

        jdbcTemplate.update("""
                INSERT INTO agendamentos_itens (agendamento_id, servico_id, nome_servico, duracao_minutos, preco_tabela)
                SELECT ?, s.id, s.nome, s.duracao_minutos, s.preco FROM servicos s WHERE s.nome = ANY (?)
                """, agendamento, servicos);
    }

    private String autenticarAdministrador() throws Exception {
        String resposta = mockMvc.perform(post("/api/autenticacao/funcionarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"admin@teste.com\", \"senha\": \"senhaAdmin123\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.tokenAcesso");
    }
}
