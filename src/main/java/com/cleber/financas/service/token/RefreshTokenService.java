package com.cleber.financas.service.token;

import com.cleber.financas.model.entity.Usuario;

import java.util.UUID;

public interface RefreshTokenService {

    /** Emite o primeiro token de uma nova família (login). */
    RefreshTokenEmitido gerar(UUID usuarioId, String clientIp, String agenteUsuario);

    /**
     * Rotaciona um token existente.
     *
     * @throws com.cleber.financas.exception.InvalidRefreshTokenException se o token estiver
     *         ausente ou expirado.
     * @throws com.cleber.financas.exception.RefreshTokenReuseException  se o token já havia
     *         sido rotacionado (reuse) — a família inteira é revogada antes de lançar.
     */
    RefreshTokenEmitido rotacionar(String rawToken, String ip, String agenteUsuario);

    /** Resolve token -> usuário, sem rotacionar. Lança se ausente/expirado/rotacionado. */
    Usuario validar(String rawToken);

    /** Logout de um dispositivo: revoga só a família daquele token. */
    void revogarFamilia(String rawToken);

    /** Logout de todos os dispositivos do usuário. */
    void revogarAllByUser(UUID usuarioId);
}
