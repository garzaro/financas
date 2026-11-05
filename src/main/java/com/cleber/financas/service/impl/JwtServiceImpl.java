package com.cleber.financas.service.impl;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.cleber.financas.model.entity.Usuario;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cleber.financas.service.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;

import javax.crypto.SecretKey;

@Service
public class JwtServiceImpl implements JwtService {

    /** Identifica o emissor — validado no parse para evitar tokens de outros sistemas. */
    private static final String ISSUER = "financas-api";

    /** HS512 exige chave de 512 bits (64 bytes). */
    private static final int MIN_KEY_BYTES = 64;

    /** Tempo de vida do access token em minutos. */
    private final String expiracao;

    private final String chaveAssinatura;
    private SecretKey secretKey;

    public JwtServiceImpl(
            @Value("${spring.app.jwtExpiration}") String expiracao,
            @Value("${spring.app.jwtSecretKey}") String chaveAssinatura) {
        this.expiracao = expiracao;
        this.chaveAssinatura = chaveAssinatura;
    }

    @PostConstruct
    void init() {
        byte[] keyBytes = chaveAssinatura.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < MIN_KEY_BYTES) {
            throw new IllegalStateException(
                    "Chave de assinatura fraca");
        }
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public String gerarToken(Usuario usuario, List<String> roles) {
        long minutos = Long.parseLong(expiracao);
        Date expiraEm = Date.from(
                LocalDateTime
                        .now()
                        .plusMinutes(minutos)
                        .atZone(ZoneId.systemDefault()).toInstant()
        );
        /**para identificar o token - serve para revogação reuso**/
        String jti = UUID.randomUUID().toString();

        return Jwts.builder()
                .id(jti)
                .expiration(expiraEm)
                .subject(usuario.getEmail())
                .claim("uuid", usuario.getUuid())
                .claim("roles", roles)
                .issuer(ISSUER)
                .issuedAt(new Date())
                .signWith(secretKey, Jwts.SIG.HS512)
                .compact();
    }

    @Override
    public Claims obterClaims(String token) throws ExpiredJwtException {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @Override
    public boolean isTokenValido(String token) {
        try {
            return obterClaims(token)
                    .getExpiration()
                    .after(new Date());
        } catch (ExpiredJwtException e) {
            return false;
        }
    }

    @Override
    public String obterLoginUsuario(String token) {
        return obterClaims(token)
                .getSubject();
    }
}
