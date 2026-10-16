package com.cleber.financas.api.dto;

import lombok.*;
/**construindo o token e retornando la para o usuario**/
@Getter
@Setter
@AllArgsConstructor
public class TokenDTO {
    /**nome do usuario**/
    private String nome;

    private String token;
}
