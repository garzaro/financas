package com.cleber.financas.api.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenCookieFactory {

    public static final String COOKIE_NAME = "refresh_token";
    private static final String COOKIE_PATH = "/api/auth";

    private final long maxAgeSeconds;
    private final boolean secure;

    public RefreshTokenCookieFactory(
            @Value("${application.security.jwt.refresh-expiration:604800000}") long refreshExpirationMs,
            @Value("${app.cookie.secure:true}") boolean secure) {
        this.maxAgeSeconds = refreshExpirationMs / 1000;
        this.secure = secure;
    }

    public ResponseCookie buildSet(String rawToken) {
        return ResponseCookie.from(COOKIE_NAME, rawToken)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Strict")
                .path(COOKIE_PATH)
                .maxAge(maxAgeSeconds)
                .build();
    }

    /** Invalida o cookie no browser zerado Max-Age. */
    public ResponseCookie buildClear() {
        return ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(secure)
                .sameSite("Strict")
                .path(COOKIE_PATH)
                .maxAge(0)
                .build();
    }
}
