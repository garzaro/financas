package com.cleber.financas.service.token;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public interface RefreshTokenService {
    /** Emite o primeiro token de uma nova família (login). */
    RefreshTokenEmitido gerar(UUID usuarioId, String ip, String agenteUsuario);

    /**
     * Rotaciona um token existente. Marca o token apresentado como ROTATED
     * (não deleta — precisa existir pra detectar reuse) e emite um novo na
     * mesma família.
     *
     * @throws InvalidRefreshTokenException se o token não existe, expirou,
     *         ou (reuse detectado) já estava ROTATED — nesse último caso a
     *         família inteira é revogada antes de lançar a exceção.
     */
    RefreshTokenEmitido rotacionar(String rawToken, String ip, String agenteUsuario);

    /** Valida um token sem rotacionar. Lança se ausente/expirado/ROTATED. */
    RefreshDadosToken validar(String rawToken); //token bruto

    /** Logout de um dispositivo: revoga só a família daquele token. */
    void revogarFamilia(String rawToken);

    /** Logout de todos os dispositivos do usuário. */
    void revogarAllByUser(UUID usuarioId);
}
