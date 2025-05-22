package com.cleber.financas.api.dto;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioAutenticacaoDTO {
    private String email;
    private String senha;
}
