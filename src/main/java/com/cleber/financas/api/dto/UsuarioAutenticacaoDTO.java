package com.cleber.financas.api.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@Builder
public class UsuarioAutenticacaoDTO {
    private String email;
    private String senha;
}
