-- Sprint 1: tabela de refresh tokens
CREATE TABLE IF NOT EXISTS financeiro.refresh_tokens (
	id          UUID         NOT NULL DEFAULT gen_random_uuid(),
	token_hash  VARCHAR(64)  NOT NULL,
	id_usuario  UUID         NOT NULL,
	criado_em   TIMESTAMP    NOT NULL DEFAULT now(),
	expira_em   TIMESTAMP    NOT NULL,
	status      VARCHAR(10)  NOT NULL DEFAULT 'ACTIVE',
	ip          VARCHAR(45),
	user_agent  VARCHAR(255),

	CONSTRAINT pk_refresh_tokens         PRIMARY KEY (id),
	CONSTRAINT uq_refresh_tokens_hash    UNIQUE (token_hash),
	CONSTRAINT fk_refresh_tokens_usuario FOREIGN KEY (id_usuario)
		REFERENCES financeiro.usuario (id_usuario) ON DELETE CASCADE,
	CONSTRAINT chk_refresh_tokens_status CHECK (status IN ('ACTIVE', 'USED', 'REVOKED', 'EXPIRED'))
);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_hash
	ON financeiro.refresh_tokens (token_hash);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_usuario
	ON financeiro.refresh_tokens (id_usuario);
