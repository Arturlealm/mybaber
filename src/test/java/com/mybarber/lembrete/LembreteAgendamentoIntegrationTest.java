package com.mybarber.lembrete;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.mybarber.compartilhado.email.EnvioEmail;
import com.mybarber.compartilhado.email.MensagemEmail;

@SpringBootTest(properties = {
        "mybarber.jwt.segredo=segredo-de-teste-com-pelo-menos-32-caracteres",
        "mybarber.lembrete.limite-diario=2",
        "mybarber.lembrete.atraso-inicial=PT1H"
})
@Testcontainers(disabledWithoutDocker = true)
class LembreteAgendamentoIntegrationTest {

    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private LembreteAgendamentoService lembreteAgendamentoService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private EnvioEmail envioEmail;

    @Test
    void deveEnviarUmLembretePorAgendamentoRespeitandoAntecedenciaELimiteDiario() {
        long barbeiro = inserirBarbeiro();
        LocalDateTime agora = LocalDateTime.now(FUSO).truncatedTo(ChronoUnit.MINUTES);
        Instant ontem = Instant.now().minus(1, ChronoUnit.DAYS);

        inserirAgendamento(inserirCliente("dentro1@teste.com"), barbeiro, agora.plusHours(2), ontem);
        inserirAgendamento(inserirCliente("dentro2@teste.com"), barbeiro, agora.plusHours(5), ontem);
        inserirAgendamento(inserirCliente("dentro3@teste.com"), barbeiro, agora.plusHours(8), ontem);
        inserirAgendamento(inserirCliente("emcimadahora@teste.com"), barbeiro, agora.plusHours(1), Instant.now());
        inserirAgendamento(inserirCliente("distante@teste.com"), barbeiro, agora.plusHours(30), ontem);

        int primeiraRodada = lembreteAgendamentoService.enviarLembretesPendentes();
        int segundaRodada = lembreteAgendamentoService.enviarLembretesPendentes();

        assertThat(primeiraRodada).isEqualTo(2);
        assertThat(segundaRodada).isZero();

        ArgumentCaptor<MensagemEmail> emails = ArgumentCaptor.forClass(MensagemEmail.class);
        verify(envioEmail, times(2)).enviar(emails.capture());
        List<String> destinatarios = emails.getAllValues().stream().map(MensagemEmail::destinatario).toList();
        assertThat(destinatarios).containsExactly("dentro1@teste.com", "dentro2@teste.com");
        assertThat(emails.getValue().assunto()).startsWith("Lembrete: seu horário");
        assertThat(emails.getValue().textoSimples()).contains("Cabelo com Barbeiro Lembrete");
    }

    private long inserirBarbeiro() {
        return jdbcTemplate.queryForObject("""
                INSERT INTO funcionarios (nome, email, telefone, senha_hash, tipo, realiza_atendimentos, filial_id)
                VALUES ('Barbeiro Lembrete', 'barbeiro.lembrete@teste.com', '11922222222', 'x', 'BARBEIRO', TRUE,
                        (SELECT id FROM filiais WHERE nome = 'Matriz'))
                RETURNING id
                """, Long.class);
    }

    private long inserirCliente(String email) {
        return jdbcTemplate.queryForObject("""
                INSERT INTO clientes (nome, email, telefone, senha_hash) VALUES ('Cliente', ?, '11911111111', 'x')
                RETURNING id
                """, Long.class, email);
    }

    private void inserirAgendamento(long cliente, long barbeiro, LocalDateTime inicio, Instant criadoEm) {
        long agendamento = jdbcTemplate.queryForObject("""
                INSERT INTO agendamentos (cliente_id, funcionario_id, filial_id, inicio, fim, status, valor_tabela, criado_em)
                VALUES (?, ?, (SELECT id FROM filiais WHERE nome = 'Matriz'), ?, ?, 'AGENDADO', 40.00, ?)
                RETURNING id
                """, Long.class, cliente, barbeiro, inicio, inicio.plusMinutes(30), Timestamp.from(criadoEm));
        jdbcTemplate.update("""
                INSERT INTO agendamentos_itens (agendamento_id, servico_id, nome_servico, duracao_minutos, preco_tabela)
                SELECT ?, id, nome, duracao_minutos, preco FROM servicos WHERE nome = 'Cabelo'
                """, agendamento);
    }
}
