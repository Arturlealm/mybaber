--liquibase formatted sql

--changeset arturlealm:003-adicionar-senha-clientes
ALTER TABLE clientes ADD COLUMN senha_hash VARCHAR(100) NOT NULL;
--rollback ALTER TABLE clientes DROP COLUMN senha_hash;
