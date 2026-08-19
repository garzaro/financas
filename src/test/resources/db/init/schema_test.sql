-- Script de inicialização do container PostgreSQL de testes (Testcontainers)
-- Cria o schema e as tabelas necessárias para os testes de integração.

CREATE SCHEMA IF NOT EXISTS financeiro;

CREATE TABLE IF NOT EXISTS financeiro.usuario (
    id_usuario       UUID         NOT NULL DEFAULT gen_random_uuid(),
    nome_completo    VARCHAR(150) NOT NULL,
    cpf              VARCHAR(11)  NOT NULL,
    nome_usuario     VARCHAR(50)  NOT NULL,
    email            VARCHAR(255) NOT NULL,
    senha            VARCHAR(255) NOT NULL,
    data_cadastro    TIMESTAMP,
    data_atualizacao TIMESTAMP    NOT NULL DEFAULT now(),
    is_ativo         BOOLEAN,
    CONSTRAINT pk_usuario PRIMARY KEY (id_usuario)
);

CREATE TABLE IF NOT EXISTS financeiro.refresh_tokens (
    id          UUID         NOT NULL DEFAULT gen_random_uuid(),
    token_hash  VARCHAR(64)  NOT NULL,
    id_usuario  UUID         NOT NULL,
    criado_em   TIMESTAMP    NOT NULL DEFAULT now(),
    expira_em   TIMESTAMP    NOT NULL,
    status      VARCHAR(10)  NOT NULL DEFAULT 'ACTIVE',
    ip          VARCHAR(45),
    user_agent  VARCHAR(255),

    CONSTRAINT pk_refresh_tokens           PRIMARY KEY (id),
    CONSTRAINT uq_refresh_tokens_hash      UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_usuario   FOREIGN KEY (id_usuario)
        REFERENCES financeiro.usuario (id_usuario) ON DELETE CASCADE,
    CONSTRAINT chk_refresh_tokens_status   CHECK (status IN ('ACTIVE', 'USED', 'REVOKED', 'EXPIRED'))
);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_hash
    ON financeiro.refresh_tokens (token_hash);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_id_usuario
    ON financeiro.refresh_tokens (id_usuario);
