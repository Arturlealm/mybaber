--liquibase formatted sql

--changeset arturlealm:011-adicionar-lembrete-agendamentos
ALTER TABLE agendamentos ADD COLUMN lembrete_enviado_em TIMESTAMPTZ;
CREATE INDEX ix_agendamentos_lembrete_pendente ON agendamentos (inicio)
    WHERE status = 'AGENDADO' AND lembrete_enviado_em IS NULL;
CREATE INDEX ix_agendamentos_lembrete_enviado_em ON agendamentos (lembrete_enviado_em)
    WHERE lembrete_enviado_em IS NOT NULL;
--rollback ALTER TABLE agendamentos DROP COLUMN lembrete_enviado_em;
