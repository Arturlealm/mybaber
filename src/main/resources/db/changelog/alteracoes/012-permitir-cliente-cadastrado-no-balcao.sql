--liquibase formatted sql

--changeset arturlealm:012-permitir-cliente-cadastrado-no-balcao
ALTER TABLE clientes ALTER COLUMN email DROP NOT NULL;
ALTER TABLE clientes ALTER COLUMN senha_hash DROP NOT NULL;
CREATE INDEX ix_clientes_telefone ON clientes (telefone);
