package com.cleber.financas.exception;

public class InvalidRefreshTokenException extends RuntimeException {
    public InvalidRefreshTokenException(String refreshTokenInválidoOuExpirado) {
    }
}
