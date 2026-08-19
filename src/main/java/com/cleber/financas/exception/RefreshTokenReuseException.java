package com.cleber.financas.exception;

public class RefreshTokenReuseException extends RuntimeException {
    public RefreshTokenReuseException(String message) {
        super(message);
    }
}
