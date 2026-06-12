package com.cleber.financas.service.impl;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.service.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

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
    
    /**ver a possibilidade de pegar o localdatetime do SISTEMA e converter para date
     * a logica seria: 
     * 
     * LocalDate localDate = LocalDate.now();
     * Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
     * 
     */

    @Override
    public String gerarToken(Usuario usuario) {
        
        Instant agora = Instant.now();
        Instant expiracao = agora.plus(expiration, ChronoUnit.MINUTES);

        return Jwts.builder()
                .subject(usuario.getEmail()) // Sequestro de sessão evitado atrelando Claims chave
                .claim("id", usuario.getId())
                .claim("cpf", usuario.getCpf())
                .claim("nome_usuario", usuario.getNomeUsuario())
                .claim("nome", usuario.getNomeCompleto())
                .claim("horaExpiracao", expiracao.toString())
                .issuer(ISSUER) // Adiciona ISSUER para garantir de onde vem o token
                .issuedAt(Date.from(agora)) //emitido em
                .expiration(Date.from(expiracao)) //expira em
                .signWith(getSigningKey()) // Usa JWS seguro padrão (HS256 ou melhor) e impede Alg None
                .compact();
    }
    
    /**O Parseamento (abertura do token) acontece aqui:**/
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
    public boolean isTokenValido(String token, UserDetails userDetails) { //vinculo duplo de seguranca
        try {
            Claims claims = obterClaims(token);
            /**validação do tempo**/
            Date dataExpiracao = claims.getExpiration();
            /**Validação rigorosa do timestamp**/
            boolean tokenNaoExpirado = dataExpiracao != null && !dataExpiracao.before(new Date());
            /**
             * Validação de Identidade (O Encaixe do UserDetails)
             * Extrai o username que foi gravado dentro do assunto (subject) do JWT
             **/
            String usernameDoToken = claims.getSubject();

            /**Comparação - username é identico ao que veio do banco**/
            boolean tokenValido = usernameDoToken != null && usernameDoToken.equals(userDetails.getUsername());

            /**Nao expirou e pertence ao usuario entao é valido**/
            return tokenNaoExpirado && tokenValido;
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
