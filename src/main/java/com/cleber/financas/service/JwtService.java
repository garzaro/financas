package com.cleber.financas.service;

import com.cleber.financas.model.entity.Usuario;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;

public interface JwtService {

    String gerarToken(Usuario usuario);

    /**
     * Claims são todas as informações que estão dentro do token
     * O token vai ter Claims de usuario
     * Claims são como se fossem chaves e valores, por exemplo:
     * Claims {
     * username = "email@email.com",
     * id = 1,
     * authorities = ["ROLE_USER"]
     * }
     **/
    Claims obterClaims(String token) throws ExpiredJwtException;
   
    boolean isTokenValido(String token);

    /**Busca o usuario logado no momento, ou seja, o dono do token**/
    String getUserLogin(String token);
}
