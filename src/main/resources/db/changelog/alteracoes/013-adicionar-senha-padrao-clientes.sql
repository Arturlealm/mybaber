--liquibase formatted sql

--changeset arturlealm:013-adicionar-senha-padrao-clientes
ALTER TABLE clientes ADD COLUMN usa_senha_padrao BOOLEAN NOT NULL DEFAULT FALSE;
--rollback ALTER TABLE clientes DROP COLUMN usa_senha_padrao;
