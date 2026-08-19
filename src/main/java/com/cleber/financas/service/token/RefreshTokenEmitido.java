package com.cleber.financas.service.token;

import java.time.Duration;
/**token issued
 * Retorno de generate()/rotate(): o valor bruto vai pro cookie HttpOnly,
 * o hash é o que fica armazenado no Redis — nunca o valor bruto.
 */
public record RefreshTokenEmitido(
        String rawToken,
        Duration ttl
) {}
