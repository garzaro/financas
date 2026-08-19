package com.cleber.financas.api.dto;

public record TokenResponseDTO(
    String accessToken,
    String tokenType
) {
    public TokenResponseDTO(String accessToken) {
        this(accessToken, "Bearer");
    }
}
