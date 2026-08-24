package com.cleber.financas.api.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Não lê mais NENHUMA property de expiração — RefreshTokenEmitido já carrega
 * o ttl calculado pelo RefreshTokenServiceImpl a partir da property correta
 *
 * app.cookie.secure continua sendo lido aqui — essa property já existe e
 * funciona (confirmada ativa no perfil de teste com valor "false").
 */

@Component
public class RefreshTokenCookieFactory {

    public static final String COOKIE_NAME = "refresh_token";
    private static final String COOKIE_PATH = "/api/auth";

    private final boolean secure;

    public RefreshTokenCookieFactory(
            @Value("${app.cookie.secure:true}") boolean secure) {
        this.secure = secure;
    }

    public ResponseCookie buildSet(String rawToken, Duration ttl) {
        return ResponseCookie.from(COOKIE_NAME, rawToken)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Strict")
                .path(COOKIE_PATH)
                .maxAge(ttl)
                .build();
    }

    /** Invalida o cookie no browser zerado Max-Age. */
    public ResponseCookie buildClear() {
        return ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(secure)
                .sameSite("Strict")
                .path(COOKIE_PATH)
                .maxAge(Duration.ZERO)
                .build();
    }
}
