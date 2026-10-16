package com.cleber.financas.service;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;

import com.cleber.financas.model.entity.Usuario;

import io.jsonwebtoken.ExpiredJwtException;

/**
 * Contrato do serviço JWT da aplicação.
 *
 * responsabilidades:
 * Geração de tokens assinados com HMAC-SHA
 * Extração e validação de claims
 * Verificação de assinatura, ISSUER e expiração
 *
 * {@link com.cleber.financas.security.JwtAuthenticationFilter} consome este serviço
 * para autenticar cada requisição.
 */
public interface JwtService {

    /**
     * Gera um accesstoken JWT com apenas o {@code username} do {@link UserDetails} como subject.
     * Usado internamente pelo filtro de autenticação e pelo endpoint de login.
     */
    String gerarToken(Usuario usuario);

    /**
     * Gera um accesstoken com claims extras adicionais ao payload.
     */
    Claims obterClaims(String token) throws ExpiredJwtException;

    /**
     * Retorna {@code true} se o accesstoken for válido, não expirado e pertencer ao
     * {@code userDetails} informado.
     */
    boolean isTokenValido(String token);

    /**
     * @throws RuntimeException se o accesstoken for inválido ou expirado
     */
     String obterLoginUsuario(String token);
}



/**
 * Gera um accesstoken JWT com apenas o {@code username} do {@link UserDetails} como subject.
 * Usado internamente pelo filtro de autenticação e pelo endpoint de login.
 */
//String gerarToken(UserDetails userDetails);

/**
 * Gera um accesstoken JWT enriquecido para um {@link Usuario}, incluindo claims extras:
 * {@code id}, {@code cpf}, {@code nome_usuario} e {@code nome}.
 */
//    String gerarToken(Usuario usuario);

/** Gera um accesstoken com claims extras adicionais ao payload. */
//String obterClaims(Map<String, Object> extraClaims, UserDetails userDetails)
//        throws ExpiredJwtException;

/**
 * Obter login do usuario (e-mail / username) armazenado no subject do accesstoken.
 *
 * @throws RuntimeException se o accesstoken for inválido ou expirado
 */
//String extrairUsernameToken(String token);

/**
 * Alias de {@link #extrairUsernameToken}, preservado para compatibilidade.
 *
 * @throws RuntimeException se o accesstoken for inválido ou expirado
 */
// String obterLoginUsuario(String token);

/**
 * Retorna {@code true} se o accesstoken for válido, não expirado e pertencer ao
 * {@code userDetails} informado.
 */
//boolean isTokenValido(String token, UserDetails userDetails);

/**
 * Parseia e retorna todas as claims do accesstoken.
 *
 * @throws ExpiredJwtException se o accesstoken estiver expirado
 * @throws RuntimeException    com mensagem "Token JWT inválido — …" para demais erros
 */
// Claims obterClaims(String token);
