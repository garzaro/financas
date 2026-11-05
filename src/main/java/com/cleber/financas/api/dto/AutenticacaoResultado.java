package com.cleber.financas.api.dto;

import com.cleber.financas.service.token.RefreshTokenEmitido;

public record AutenticacaoResultado(
        TokenResponseDTO tokens,
        RefreshTokenEmitido refresh
) {
}
