package com.cleber.financas.api.dto;

import java.time.Instant;
import java.util.UUID;
/**token data
 * Dados de um refresh token ativo, persistidos no Redis.
 * novoStatus ACTIVE  -> token válido, pode ser apresentado para rotação.
 * novoStatus ROTATED -> token já foi trocado; se apresentado novamente,
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
                novoStatus,
                ip,
                agenteUsuario,
                emitidoEm
        );
    }
}
