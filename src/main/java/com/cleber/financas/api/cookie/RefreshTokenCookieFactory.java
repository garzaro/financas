package com.cleber.financas.api.cookie;

import java.time.Duration;

import org.springframework.http.ResponseCookie;

public interface RefreshTokenCookieFactory {

    String COOKIE_NAME = "refresh_token";

    ResponseCookie buildSet(String token, Duration ttl);

    ResponseCookie buildClear();
}
