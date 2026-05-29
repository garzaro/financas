package com.cleber.financas.service.impl;

import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JwtServiceImpl implements JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secret;

    @Value("${application.security.jwt.expiration}") //:30
    private Long expiration;

    // Constante para definir e validar a origem do Token (evita vazamento de escopo)
    private static final String ISSUER = "financas-api";

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        // O hmacShaKeyFor cria a chave apropriada. O JJWT escolhe a assinatura forte baseada na chave.
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public String gerarToken(Usuario usuario) {
        /**ver a possibilidade de peagr o localdatetime do SISTEMA e converter para date
         * a logica seria: 
         * 
         * LocalDate localDate = LocalDate.now();
         * Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
         * 
         */
        Instant agora = Instant.now();
        Instant expiracao = agora.plus(expiration, ChronoUnit.MINUTES);

        return Jwts.builder()
                .subject(usuario.getEmail()) // Sequestro de sessão evitado atrelando Claims chave
                .claim("id", usuario.getId())
                .claim("cpf", usuario.getCpf())
                .claim("nome_usuario", usuario.getNomeUsuario())
                .claim("nome", usuario.getNome())
                .issuer(ISSUER) // Adiciona ISSUER para garantir de onde vem o token
                .issuedAt(Date.from(agora))
                .expiration(Date.from(expiracao))
                .signWith(getSigningKey()) // Usa JWS seguro padrão (HS256 ou melhor) e impede Alg None
                .compact();
    }

    @Override
    public Claims obterClaims(String token) throws ExpiredJwtException {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey()) // Obriga a verificação com a chave, rejeita token sem assinatura (Alg None)
                    .requireIssuer(ISSUER) // Reivindicação rigorosa: Garante que foi nossa API que gerou
                    .build()
                    .parseSignedClaims(token) // Exige explicitamente um token ASSINADO, blindagem total contra Alg None
                    .getPayload();
        } catch (ExpiredJwtException e) {
            // Repassado para tratar na camada de filtro/segurança de forma específica
            throw e;
        } catch (JwtException | IllegalArgumentException e) {
            // Qualquer outra adulteração (assinatura inválida, Claims faltando, malformado)
            throw new RuntimeException("Token JWT inválido, adulterado ou malformado.", e);
        }
    }

    @Override
    public boolean isTokenValido(String token) {
        try {
            Claims claims = obterClaims(token);
            Date dataExpiracao = claims.getExpiration();
            // Validação rigorosa do timestamp
            return dataExpiracao != null && !dataExpiracao.before(new Date());
        } catch (RuntimeException e) { // JwtException | IllegalArgumentException | 
            return false;
        }
    }

    @Override
    public String getUserLogin(String token) {
        Claims claims = obterClaims(token);
        return claims.getSubject();
    }
}
