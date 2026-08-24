package com.cleber.financas.service.token;

import com.cleber.financas.model.entity.Usuario;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public interface RefreshTokenService {
    /** Emite o primeiro token de uma nova família (login). */
    RefreshTokenEmitido gerar(UUID usuarioId, String clientIp, String agenteUsuario);

    /**
     * Rotaciona um token existente.
     * @throws InvalidRefreshTokenException se ausente/expirado, ou (reuse
     *         detectado) se já estava ROTACIONADO — nesse caso a família
     *         inteira é revogada antes de lançar.
     */
    RefreshTokenEmitido rotacionar(String rawToken, String ip, String agenteUsuario);

    /**
     * Resolve token -> usuário, sem rotacionar.
     * Valida um token sem rotacionar. Lança se ausente/expirado/ROTATED. /
    RefreshDadosToken validar(String rawToken);*/ //token bruto

    Usuario validar(String rawToken);

    /** Logout de um dispositivo: revoga só a família daquele token. */
    void revogarFamilia(String rawToken);

    /** Logout de todos os dispositivos do usuário. */
    void revogarAllByUser(UUID usuarioId);
}
