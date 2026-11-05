package com.cleber.financas.api.cookie;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class DefaultRefreshTokenCookieFactory implements RefreshTokenCookieFactory {

    private static final String COOKIE_PATH = "/api/auth";

    private final boolean secure;

    public DefaultRefreshTokenCookieFactory(
            @Value("${app.cookie.secure:true}") boolean secure) {
        this.secure = secure;
    }

    @Override
    public ResponseCookie buildSet(String token, Duration ttl) {
        return ResponseCookie.from(COOKIE_NAME, token)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Strict")
                .path(COOKIE_PATH)
                .maxAge(ttl)
                .build();
    }

    @Override
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
