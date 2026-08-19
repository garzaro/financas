package com.cleber.financas.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.service.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtServiceImpl implements JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtServiceImpl.class);

    /** Identifica o emissor — validado no parse para evitar tokens de outros sistemas. */
    private static final String ISSUER = "financas-api";

    private final SecretKey signingKey;
    private final long expirationMs;
    private final long expirationRefeshToken;

    public JwtServiceImpl(
            @Value("${spring.app.jwtSecretKey}") String secret,
            @Value("${spring.app.jwtExpirationMs}") long expirationMs,
            @Value("${spring.app.jwtRefreshExpirationMs}") Integer expirationRefeshToken
    ) {
//        if (secret == null || secret.isBlank()) {
//            throw new IllegalArgumentException("JWT Secret Key não configurada. Verifique as variáveis de ambiente ou o arquivo .env.");
//        }
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMs = expirationMs;
        this.expirationRefeshToken = expirationRefeshToken;
    }

    @Override
    public String extrairUsernameToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extrairTodasClaims(token);
        return claimsResolver.apply(claims);
    }

    /**ver a possibilidade de pegar o localdatetime do SISTEMA e converter para date
     * a logica seria:
     *
     * LocalDate localDate = LocalDate.now();
     * Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
     *
     */

    @Override
    public String gerarToken(UserDetails userDetails) {
        return gerarTokenComClaims(new HashMap<>(), userDetails);
    }

    public String gerarTokenComClaims(Map<String, Object> extraClaims, UserDetails userDetails) {
        Date agora = new Date();
        //Date expiracao = Date.from(agora.plus(expirationMs, ChronoUnit.MILLIS));
        Date expiracao = new Date(agora.getTime() + expirationMs);

        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername()) // Sequestro de sessão evitado atrelando Claims chave
                .claim("horaExpiracao", expiracao.toString()) //?
                .issuer(ISSUER) // Adiciona ISSUER para garantir de onde vem o accesstoken
                .issuedAt(agora) //emitido em
                .expiration(expiracao) //expira em
                .signWith(signingKey) // Usa JWS seguro padrão (HS256 ou melhor) e impede Alg None
                .compact();
    }

    public boolean isTokenValido(String token, UserDetails userDetails) {
        try {
        	/**O email no accesstoken deve bater com o carregado pelo banco e nao expirado**/
            final String username = extrairUsernameToken(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpirado(token));
        } catch (ExpiredJwtException ex) {
            logger.warn("Token expirado ou inválido: {}", ex.getClaims().getSubject() +ex.getMessage());
            return false;
        }
    }

    private boolean isTokenExpirado(String token) {
        return extrairExpiracao(token).before(new Date());
    }

    private Date extrairExpiracao(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extrairTodasClaims(String token) {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
    }

	@Override
	public Claims obterClaims(String token) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String gerarToken(Usuario usuario) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUserLogin(String token) {
		// TODO Auto-generated method stub
		return null;
	}



//	@Override
//	public String getUserLogin(String accesstoken) {
//		// TODO Auto-generated method stub
//		return null;
//	}
}





    /**O Parseamento (abertura do accesstoken) acontece aqui:**
    public Claims obterClaims(String accesstoken) throws ExpiredJwtException {
        return extractAllClaims(accesstoken);
    }

    @Override
    public String getUserLogin(String accesstoken) {
        Claims claims = obterClaims(accesstoken);
        return claims.getSubject();
    }
}

/**
 * VER A POSSIBLIDADE DE MESMO USANDO USERDETAILS CHAMAR MAI DE UMA CLAIMS NAO SO O EMAIL E VER
 * VIABILIDADE TECNICA TAMBEM
public String gerarTokenComClaims(Map<String, Object> extraClaims, Usuario usuario) {

        Date agora = new Date();
        //Date expiracao = Date.from(agora.plus(expirationMs, ChronoUnit.MILLIS));
        Date expiracao = new Date(agora.getTime() + expirationMs);

        return Jwts.builder()
                .subject(usuario.getEmail()) // Sequestro de sessão evitado atrelando Claims chave
                .claim("id", usuario.getId())
                .claim("cpf", usuario.getCpf())
                .claim("nome_usuario", usuario.getNomeUsuario())
                .claim("nome", usuario.getNomeCompleto())
                .claim("horaExpiracao", expiracao.toString())
                .issuer(ISSUER) // Adiciona ISSUER para garantir de onde vem o accesstoken
                .issuedAt(Date.from(agora)) //emitido em
                .expiration(Date.from(expiracao)) //expira em
                .signWith(getSigningKey()) // Usa JWS seguro padrão (HS256 ou melhor) e impede Alg None
                .compact();
    }
**/

/**
 * private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        // O hmacShaKeyFor cria a chave apropriada. O JJWT escolhe a assinatura forte baseada na chave.
        return Keys.hmacShaKeyFor(keyBytes);
    }
 * **/


/**
 * @Override
    public boolean isTokenValido(String accesstoken, UserDetails userDetails) { //vinculo duplo de seguranca
        try {
            Claims claims = obterClaims(accesstoken);
            /**validação do tempo**
            Date dataExpiracao = claims.getExpiration();
            /**Validação rigorosa do timestamp**
            boolean tokenNaoExpirado = dataExpiracao != null && !dataExpiracao.before(new Date());
            /**
             * Validação de Identidade (O Encaixe do UserDetails)
             * Extrai o username que foi gravado dentro do assunto (subject) do JWT
             **
            String usernameDoToken = claims.getSubject();

            /**Comparação - username é identico ao que veio do banco**
            boolean tokenValido = usernameDoToken != null && usernameDoToken.equals(userDetails.getUsername());

            /**Nao expirou e pertence ao usuario entao é valido**
            return tokenNaoExpirado && tokenValido;
        } catch (RuntimeException e) { // JwtException | IllegalArgumentException |
            return false;
    }
}

 * **/
