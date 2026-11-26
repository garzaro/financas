package com.cleber.financas.api.dto;

public record TokenResponseDTO(
        String accessToken,
        String tokenType,
        String nomeUsuario
        ) {
    public TokenResponseDTO(String accessToken) {
        this(accessToken, "Bearer", null);
    }
}
