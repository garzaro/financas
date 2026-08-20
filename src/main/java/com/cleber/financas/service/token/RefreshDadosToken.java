package com.cleber.financas.service.token;

import java.time.Instant;
import java.util.UUID;
/**token data
 * Dados de um refresh token ativo, persistidos no Redis.
 * status ACTIVE  -> token válido, pode ser apresentado para rotação.
 * status ROTATED -> token já foi trocado; se apresentado novamente,
 *                   é sinal de reuse (token roubado) -> revoga a família inteira.
 */
public record RefreshDadosToken(
        UUID usuarioId,
        UUID familiaId,
        Status status,
        String ip,
        String agenteUsuario,
        Instant emitidoEm //issuedAt
) {
    public enum Status { ATIVO, ROTACIONADO }

    public RefreshDadosToken comStatus(Status novoStatus) {
        return new RefreshDadosToken(
                usuarioId,
                familiaId,
                status,
                ip,
                agenteUsuario,
                emitidoEm
        );
    }
}
