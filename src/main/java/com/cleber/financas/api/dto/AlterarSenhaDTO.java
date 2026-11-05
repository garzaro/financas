package com.cleber.financas.api.dto;

import com.cleber.financas.api.validation.SenhaConstraints;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AlterarSenhaDTO(
        @NotBlank(message = "{usuario.senha.obrigatoria}")
        String senhaAtual,

        @NotBlank(message = "{usuario.senha.obrigatoria}")
        @Size(min = SenhaConstraints.TAMANHO_MINIMO, message = "{usuario.senha.tamanho.minimo}")
        String novaSenha
) {
}
