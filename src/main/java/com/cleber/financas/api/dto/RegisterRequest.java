package com.cleber.financas.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = "{usuario.nome.completo.obrigatorio}")
    String nome,

    @NotBlank(message = "{usuario.email.obrigatorio}")
    @Email(message = "{usuario.email.invalido}")
    String email,

    @NotBlank(message = "{usuario.senha.obrigatoria}")
    @Size(min = 6, message = "{usuario.senha.tamanho.minimo}")
    String senha
) {}
