package com.cleber.financas.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.service.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@DisplayName("JwtService — geração, expiração e validação do access token")
class JwtServiceTest {

    private static final String SECRET =
            "test-secret-key-for-hs512-with-at-least-64-bytes-0123456789-abcdef";
    private static final String EXPIRATION_MINUTES = "60";
    private static final String ISSUER = "financas-api";

    private JwtService jwtService;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        JwtServiceImpl impl = new JwtServiceImpl(EXPIRATION_MINUTES, SECRET);
        impl.init();
        jwtService = impl;

        usuario = Usuario.builder()
                .uuid(UUID.randomUUID())
                .email("cleber@gmail.com")
                .build();
    }

    @Test
    @DisplayName("gera token com subject, uuid, issuer e expiração futura")
    void deveGerarTokenValido() {
        String token = jwtService.gerarToken(usuario);

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
        assertThat(jwtService.obterLoginUsuario(token)).isEqualTo(usuario.getEmail());

        Claims claims = jwtService.obterClaims(token);
        assertThat(claims.get("uuid", String.class)).isEqualTo(usuario.getUuid().toString());
        assertThat(claims.getIssuer()).isEqualTo(ISSUER);
        assertThat(claims.getExpiration()).isAfter(new Date());
    }

    @Test
    @DisplayName("rejeita token expirado")
    void deveRejeitarTokenExpirado() {
        String expirado = tokenAssinado(
                Date.from(Instant.now().minus(2, ChronoUnit.HOURS)),
                Date.from(Instant.now().minus(1, ChronoUnit.HOURS)));

        assertThat(jwtService.isTokenValido(expirado)).isFalse();
        assertThatThrownBy(() -> jwtService.obterLoginUsuario(expirado))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    @DisplayName("rejeita token adulterado")
    void deveRejeitarTokenAdulterado() {
        String token = jwtService.gerarToken(usuario);
        String adulterado = token.substring(0, token.length() - 3) + "XYZ";

        assertThatThrownBy(() -> jwtService.obterLoginUsuario(adulterado))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("rejeita token assinado com outra chave")
    void deveRejeitarTokenComOutraChave() {
        SecretKey outraChave = Keys.hmacShaKeyFor(
                "outra-chave-de-testes-com-pelo-menos-64-bytes-para-o-hs512-0123456789"
                        .getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject(usuario.getEmail())
                .issuer(ISSUER)
                .expiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .signWith(outraChave)
                .compact();

        assertThatThrownBy(() -> jwtService.obterLoginUsuario(token))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("rejeita token com algoritmo none")
    void deveRejeitarTokenAlgNone() {
        String payload = jwtService.gerarToken(usuario).split("\\.")[1];
        String headerNone = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"none\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> jwtService.obterLoginUsuario(headerNone + "." + payload + "."))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("falha na inicialização com chave menor que 64 bytes")
    void deveRejeitarChaveCurta() {
        JwtServiceImpl impl = new JwtServiceImpl(EXPIRATION_MINUTES, "chave-curta");

        assertThatThrownBy(impl::init).isInstanceOf(IllegalStateException.class);
    }

    private String tokenAssinado(Date issuedAt, Date expiration) {
        return Jwts.builder()
                .subject(usuario.getEmail())
                .issuer(ISSUER)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}
