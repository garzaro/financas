package com.cleber.financas.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.cleber.financas.service.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.app.jwtSecretKey=chave-super-secreta-de-testes-com-256bits-minimos-ok",
    "spring.app.jwtExpirationMs=60000",
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;INIT=CREATE SCHEMA IF NOT EXISTS FINANCEIRO",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false"
})
@DisplayName("JwtService — contrato")
class JwtServiceTest {

    private static final String SECRET = "chave-super-secreta-de-testes-com-256bits-minimos-ok";

    @Autowired
    private JwtService jwtService;

    private UserDetails userDetailsBase;

    @BeforeEach
    void setUp() {
        userDetailsBase = User.withUsername("cleber@gmail.com")
                .password("senha-hash-qualquer")
                .authorities("USER")
                .build();
    }

    @Test
    @DisplayName("deve gerar token válido com os dados do UserDetails")
    void deveGerarTokenValido() {
        String token = jwtService.gerarToken(userDetailsBase);

        assertThat(token).isNotNull().isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
        assertThat(jwtService.extrairUsernameToken(token)).isEqualTo("cleber@gmail.com");
    }

    @Test
    @DisplayName("deve gerar tokens diferentes para o mesmo usuário em momentos distintos")
    void deveGerarTokensDiferentesParaMesmoUsuario() throws InterruptedException {
        String tokenA = jwtService.gerarToken(userDetailsBase);
        Thread.sleep(1_100);
        String tokenB = jwtService.gerarToken(userDetailsBase);

        assertThat(tokenA).isNotEqualTo(tokenB);
    }

    @Test
    @DisplayName("deve incluir claims extras no payload")
    void deveIncluirClaimsExtras() {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("cpf", "12345678900");
        extraClaims.put("nome", "Cleber Teste");

        String token = jwtService.gerarTokenComClaims(extraClaims, userDetailsBase);
        Claims claims = parse(token);

        assertThat(claims.getSubject()).isEqualTo("cleber@gmail.com");
        assertThat(claims.get("cpf", String.class)).isEqualTo("12345678900");
        assertThat(claims.get("nome", String.class)).isEqualTo("Cleber Teste");
        assertThat(claims.getExpiration()).isAfter(new Date());
    }

    @Test
    @DisplayName("deve validar token do mesmo usuário")
    void deveValidarTokenValido() {
        String token = jwtService.gerarToken(userDetailsBase);

        assertThat(jwtService.isTokenValido(token, userDetailsBase)).isTrue();
    }

    @Test
    @DisplayName("deve rejeitar token de usuário diferente")
    void deveRejeitarTokenDeUsuarioDiferente() {
        String token = jwtService.gerarToken(userDetailsBase);
        UserDetails outroUser = User.withUsername("outro@gmail.com")
                .password("outra-senha")
                .authorities("USER")
                .build();

        assertThat(jwtService.isTokenValido(token, outroUser)).isFalse();
    }

    @Test
    @DisplayName("deve rejeitar token expirado")
    void deveRejeitarTokenExpirado() {
        String tokenExpirado = criarTokenExpirado();

        assertThat(jwtService.isTokenValido(tokenExpirado, userDetailsBase)).isFalse();
        assertThatThrownBy(() -> jwtService.extrairUsernameToken(tokenExpirado))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    @DisplayName("deve lançar exceção para token adulterado")
    void deveLancarExcecaoParaTokenAdulterado() {
        String token = jwtService.gerarToken(userDetailsBase);
        String adulterado = token.substring(0, token.length() - 3) + "XYZ";

        assertThatThrownBy(() -> jwtService.extrairUsernameToken(adulterado))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("deve lançar exceção para token malformado ou vazio")
    void deveLancarExcecaoParaTokenMalformadoOuVazio() {
        assertThatThrownBy(() -> jwtService.extrairUsernameToken("lixo.total.aqui"))
                .isInstanceOf(RuntimeException.class);
        assertThatThrownBy(() -> jwtService.extrairUsernameToken(""))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("deve lançar exceção para token com chave diferente")
    void deveLancarExcecaoParaTokenComChaveDiferente() {
        String token = criarTokenComChaveDiferente();

        assertThatThrownBy(() -> jwtService.extrairUsernameToken(token))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("deve rejeitar token com algoritmo none")
    void deveRejeitarTokenAlgNone() {
        String token = criarTokenAlgNone();

        assertThatThrownBy(() -> jwtService.extrairUsernameToken(token))
                .isInstanceOf(RuntimeException.class);
    }

    private Claims parse(String token) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String criarTokenExpirado() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        Instant agora = Instant.now();

        return Jwts.builder()
                .subject(userDetailsBase.getUsername())
                .issuer("financas-api")
                .issuedAt(Date.from(agora.minus(2, ChronoUnit.HOURS)))
                .expiration(Date.from(agora.minus(1, ChronoUnit.HOURS)))
                .signWith(key)
                .compact();
    }

    private String criarTokenComChaveDiferente() {
        String outraChave = "chave-completamente-diferente-e-invalida-256bits!!";
        SecretKey key = Keys.hmacShaKeyFor(outraChave.getBytes(StandardCharsets.UTF_8));
        Instant agora = Instant.now();

        return Jwts.builder()
                .subject(userDetailsBase.getUsername())
                .issuer("financas-api")
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plus(60, ChronoUnit.MINUTES)))
                .signWith(key)
                .compact();
    }

    private String criarTokenAlgNone() {
        String tokenReal = jwtService.gerarToken(userDetailsBase);
        String payloadBase64 = tokenReal.split("\\.")[1];

        String headerAlgNone = java.util.Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString("{\"alg\":\"none\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));

        return headerAlgNone + "." + payloadBase64 + ".";
    }
}
