package com.cleber.financas.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.service.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Testes de integração do {@link JwtServiceImpl}, cobrindo:
 * <ul>
 *   <li>Geração e estrutura do token</li>
 *   <li>Extração e integridade das claims</li>
 *   <li>Validação completa (assinatura, ISSUER, expiração, subject)</li>
 *   <li>Cenários de falha e ataques conhecidos (Alg None, chave trocada)</li>
 *   <li>Round-trip: gerar → parsear → validar</li>
 * </ul>
 */
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
@DisplayName("JwtService — Testes de segurança completos")
class JwtServiceTest {

    /**Constantes compartilhadas com o helper de fabricação de tokens**/

    /**
     * Segredo alinhado com a propriedade definida em {@code @TestPropertySource}.
     * Usado pelos helpers para fabricar tokens "do lado de fora" do serviço.
     */
    private static final String SECRET = "chave-super-secreta-de-testes-com-256bits-minimos-ok";

//  O ISSUER (que significa "emissor", em inglês) é um claim padrão do
//  JWT que identifica qual sistema, aplicação ou serviço gerou aquele
//  token específico.
//  O JSON interno do token fica mais ou menos assim:
//  Payload
//{
//  "iss": "project",
//  "sub": "usuario@email.com",
//  "exp": 1717372800
//}
    private static final String ISSUER = "financas-api";

    // -------------------------------------------------------------------------
    // Fixtures
    // -------------------------------------------------------------------------

    @Autowired
    private JwtService jwtService;

    /** Entidade de domínio — fonte de dados para os claims enriquecidos. */
    private Usuario usuarioBase;

    /**
     * {@link UserDetails} simples que espelha o e-mail do {@link #usuarioBase}.
     * Usado nos métodos de validação que recebem {@code UserDetails}.
     */
    private UserDetails userDetailsBase;

    @BeforeEach
    void setUp() {
        usuarioBase = Usuario.builder()
                .uuid(UUID.randomUUID())
                .nomeCompleto("Cleber Teste")
                .nomeUsuario("cleber")
                .email("cleber@gmail.com")
                .cpf("12345678900")
                .senha("senha-hash-qualquer")
                .build();

        userDetailsBase = User.builder()
                .username(usuarioBase.getEmail())
                .password(usuarioBase.getSenha())
                .build();
    }

    // =========================================================================
    // 1. Geração do token
    // =========================================================================

    @Nested
    @DisplayName("1 — gerarToken()")
    class GerarToken {

        @Test
        @DisplayName("deve retornar uma string JWT não nula e não vazia")
        void deveRetornarTokenNaoNuloNaoVazio() {
            String token = jwtService.gerarToken(usuarioBase);
            assertThat(token).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("deve gerar token com 3 partes separadas por ponto (header.payload.signature)")
        void deveGerarTokenComTresPartes() {
            String token = jwtService.gerarToken(usuarioBase);
            assertThat(token.split("\\.")).hasSize(3);
        }

        @Test
        @DisplayName("dois tokens consecutivos do mesmo usuário devem ser diferentes (issuedAt distinto)")
        void doisTokensDevemseTerTimestampDistintos() throws InterruptedException {
            String tokenA = jwtService.gerarToken(usuarioBase);
            Thread.sleep(1_100); // precisão de 1 segundo no claim iat
            String tokenB = jwtService.gerarToken(usuarioBase);
            assertThat(tokenA).isNotEqualTo(tokenB);
        }
    }

    /**2. CLAIMS**/
    @Nested
    @DisplayName("2 — obterClaims() — conteúdo do payload")
    class ObterClaimsConteudo {

        @Test
        @DisplayName("subject deve ser o e-mail do usuário")
        void subjectDeveSerEmail() {
            Claims claims = jwtService.obterClaims(jwtService.gerarToken(usuarioBase));
            assertThat(claims.getSubject()).isEqualTo("cleber@gmail.com");
        }

        @Test
        @DisplayName("claim 'id' deve corresponder ao UUID do usuário")
        void claimIdDeveCorresponderAoUsuario() {
            Claims claims = jwtService.obterClaims(jwtService.gerarToken(usuarioBase));
            // JJWT serializa UUID como String
            assertThat(UUID.fromString(claims.get("id", String.class)))
                    .isEqualTo(usuarioBase.getUuid());
        }

        @Test
        @DisplayName("claim 'cpf' deve estar presente e correto")
        void claimCpfDeveEstarPresente() {
            Claims claims = jwtService.obterClaims(jwtService.gerarToken(usuarioBase));
            assertThat(claims.get("cpf", String.class)).isEqualTo("12345678900");
        }

        @Test
        @DisplayName("claim 'nome_usuario' deve estar presente e correto")
        void claimNomeUsuarioDeveEstarPresente() {
            Claims claims = jwtService.obterClaims(jwtService.gerarToken(usuarioBase));
            assertThat(claims.get("nome_usuario", String.class)).isEqualTo("cleber");
        }

        @Test
        @DisplayName("claim 'nome' deve estar presente e correto")
        void claimNomeDeveEstarPresente() {
            Claims claims = jwtService.obterClaims(jwtService.gerarToken(usuarioBase));
            assertThat(claims.get("nome", String.class)).isEqualTo("Cleber Teste");
        }

        @Test
        @DisplayName("claim 'horaExpiracao' deve estar presente e não vazia")
        void claimHoraExpiracaoDeveEstarPresente() {
            Claims claims = jwtService.obterClaims(jwtService.gerarToken(usuarioBase));
            assertThat(claims.get("horaExpiracao", String.class))
                    .isNotNull()
                    .isNotBlank();
        }

        @Test
        @DisplayName("issuer deve ser 'financas-api'")
        void issuerDeveSerFinancasApi() {
            Claims claims = jwtService.obterClaims(jwtService.gerarToken(usuarioBase));
            assertThat(claims.getIssuer()).isEqualTo(ISSUER);
        }

        @Test
        @DisplayName("issuedAt deve ser anterior ou igual ao momento da verificação")
        void issuedAtDeveSerAnteriorOuIgualAoMomentoAtual() {
            Claims claims = jwtService.obterClaims(jwtService.gerarToken(usuarioBase));
            assertThat(claims.getIssuedAt()).isBeforeOrEqualTo(new Date());
        }

        @Test
        @DisplayName("expiration deve ser posterior ao issuedAt")
        void expirationDeveSerPosteriorAoIssuedAt() {
            Claims claims = jwtService.obterClaims(jwtService.gerarToken(usuarioBase));
            assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
        }

        @Test
        @DisplayName("expiration deve ser posterior ao momento atual")
        void expirationDeveSerPosteriorAoMomentoAtual() {
            Claims claims = jwtService.obterClaims(jwtService.gerarToken(usuarioBase));
            assertThat(claims.getExpiration()).isAfter(new Date());
        }

        @Test
        @DisplayName("token com expiração em 30 s deve ser válido antes de expirar")
        void tokenCom30SegundosDeveSerValidoAntesDeExpirar() {
            String token = criarTokenComExpiracaoEmSegundos(30);
            assertThat(jwtService.isTokenValido(token, userDetailsBase)).isTrue();
            assertThat(jwtService.obterClaims(token).getExpiration()).isAfter(new Date());
        }
    }

    /**3. getUserLogin**/

    @Nested
    @DisplayName("3 — getUserLogin()")
    class GetUserLogin {

        @Test
        @DisplayName("deve retornar o e-mail do dono do token")
        void deveRetornarEmailDoDonoDoToken() {
            String token = jwtService.gerarToken(usuarioBase);
            assertThat(jwtService.getUserLogin(token)).isEqualTo("cleber@gmail.com");
        }

        @Test
        @DisplayName("tokens de usuários distintos devem retornar logins distintos")
        void deveDiferenciarTokensDeUsuariosDistintos() {
            Usuario distinto = Usuario.builder()
                    .uuid(UUID.randomUUID())
                    .nomeCompleto("Usuário Distinto")
                    .nomeUsuario("distinto")
                    .email("distinto@gmail.com")
                    .cpf("98765432100")
                    .senha("hash-distinta")
                    .build();
            /**acção/execução**/
            String tokenA = jwtService.gerarToken(usuarioBase);
            String tokenB = jwtService.gerarToken(distinto);

            assertThat(jwtService.getUserLogin(tokenA)).isEqualTo("cleber@gmail.com");
            assertThat(jwtService.getUserLogin(tokenB)).isEqualTo("distinto@gmail.com");
            assertThat(jwtService.getUserLogin(tokenA)).isNotEqualTo(jwtService.getUserLogin(tokenB));
        }
    }

    /**4. isTokenValido — caminho feliz**/

    @Nested
    @DisplayName("4 — isTokenValido() — token válido")
    class IsTokenValidoCaminhoFeliz {

        @Test
        @DisplayName("token recém-gerado deve ser válido")
        void tokenRecenteDeveSerValido() {
            String token = jwtService.gerarToken(usuarioBase);
            assertThat(jwtService.isTokenValido(token, userDetailsBase)).isTrue();
        }

        @Test
        @DisplayName("token de outro usuário também deve ser válido para aquele usuário")
        void tokenDeOutroUsuarioDeveSerValidoParaAqueleUsuario() {
            Usuario outro = Usuario.builder()
                    .uuid(UUID.randomUUID())
                    .nomeCompleto("Outro Usuário")
                    .nomeUsuario("outro")
                    .email("outro@gmail.com")
                    .cpf("11122233300")
                    .senha("outra-senha")
                    .build();
            UserDetails outroDetails = User.builder()
                    .username(outro.getEmail())
                    .password(outro.getSenha())
                    .build();

            assertThat(jwtService.isTokenValido(jwtService.gerarToken(outro), outroDetails)).isTrue();
        }
    }

    /**5. isTokenValido — cenários de falha / segurança**/

    @Nested
    @DisplayName("5 — isTokenValido() — cenários de falha")
    class IsTokenValidoCenariosDeFalha {

        @Test
        @DisplayName("token expirado deve retornar false")
        void tokenExpiradoDeveRetornarFalse() {
            assertThat(jwtService.isTokenValido(criarTokenExpirado(), userDetailsBase)).isFalse();
        }

        @Test
        @DisplayName("token com assinatura adulterada deve retornar false")
        void tokenAdulteradoDeveRetornarFalse() {
            String token     = jwtService.gerarToken(usuarioBase);
            String adulterado = token.substring(0, token.length() - 3) + "XYZ";
            assertThat(jwtService.isTokenValido(adulterado, userDetailsBase)).isFalse();
        }

        @Test
        @DisplayName("token completamente malformado deve retornar false")
        void tokenMalformadoDeveRetornarFalse() {
            assertThat(jwtService.isTokenValido("isso.nao.e.um.token", userDetailsBase)).isFalse();
        }

        @Test
        @DisplayName("token vazio deve retornar false")
        void tokenVazioDeveRetornarFalse() {
            assertThat(jwtService.isTokenValido("", userDetailsBase)).isFalse();
        }

        @Test
        @DisplayName("token null deve retornar false")
        void tokenNullDeveRetornarFalse() {
            assertThat(jwtService.isTokenValido(null, userDetailsBase)).isFalse();
        }

        @Test
        @DisplayName("token assinado com chave diferente deve retornar false")
        void tokenAssinadoComChaveDiferenteDeveRetornarFalse() {
            assertThat(jwtService.isTokenValido(criarTokenComChaveDiferente(), userDetailsBase)).isFalse();
        }

        @Test
        @DisplayName("token com ISSUER diferente deve retornar false")
        void tokenComIssuerDiferenteDeveRetornarFalse() {
            assertThat(jwtService.isTokenValido(criarTokenComIssuerDiferente(), userDetailsBase)).isFalse();
        }

        @Test
        @DisplayName("token sem ISSUER deve retornar false (ISSUER é obrigatório)")
        void tokenSemIssuerDeveRetornarFalse() {
            assertThat(jwtService.isTokenValido(criarTokenSemIssuer(), userDetailsBase)).isFalse();
        }
    }

    /**6. obterClaims — cenários de exceção**/

    @Nested
    @DisplayName("6 — obterClaims() — cenários de exceção")
    class ObterClaimsCenariosDeExcecao {

        @Test
        @DisplayName("token expirado deve lançar ExpiredJwtException")
        void tokenExpiradoDeveLancarExpiredJwtException() {
            assertThatThrownBy(() -> jwtService.obterClaims(criarTokenExpirado()))
                    .isInstanceOf(ExpiredJwtException.class);
        }

        @Test
        @DisplayName("token adulterado deve lançar RuntimeException com mensagem padronizada")
        void tokenAdulteradoDeveLancarRuntimeException() {
            String token     = jwtService.gerarToken(usuarioBase);
            String adulterado = token.substring(0, token.length() - 3) + "ZZZ";
            assertThatThrownBy(() -> jwtService.obterClaims(adulterado))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Token JWT inválido");
        }

        @Test
        @DisplayName("token malformado deve lançar RuntimeException com mensagem padronizada")
        void tokenMalformadoDeveLancarRuntimeException() {
            assertThatThrownBy(() -> jwtService.obterClaims("lixo.total.aqui"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Token JWT inválido");
        }

        @Test
        @DisplayName("token assinado com outra chave deve lançar RuntimeException")
        void tokenDeOutraChaveDeveLancarRuntimeException() {
            assertThatThrownBy(() -> jwtService.obterClaims(criarTokenComChaveDiferente()))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Token JWT inválido");
        }

        @Test
        @DisplayName("token com ISSUER errado deve lançar RuntimeException")
        void tokenComIssuerErradoDeveLancarRuntimeException() {
            assertThatThrownBy(() -> jwtService.obterClaims(criarTokenComIssuerDiferente()))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Token JWT inválido");
        }
    }

    /**7. Proteção contra Alg None (ataque crítico de JWT)**/

    @Nested
    @DisplayName("7 — Proteção contra ataque 'Algorithm None'")
    class ProtecaoAlgNone {

        /**
         * O ataque "alg:none" consiste em enviar um JWT cujo header declara
         * algoritmo "none" e sem assinatura, tentando burlar a verificação.
         * O JJWT 0.12.x rejeita isso em parseSignedClaims() por exigir
         * explicitamente um token assinado (JWS).
         */
        @Test
        @DisplayName("token com alg=none deve ser rejeitado — isTokenValido retorna false")
        void tokenAlgNoneDeveSerRejeitadoPorIsTokenValido() {
            assertThat(jwtService.isTokenValido(criarTokenAlgNone(), userDetailsBase)).isFalse();
        }

        @Test
        @DisplayName("token com alg=none deve lançar RuntimeException em obterClaims")
        void tokenAlgNoneDeveLancarExcecaoEmObterClaims() {
            assertThatThrownBy(() -> jwtService.obterClaims(criarTokenAlgNone()))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    /**8. Integridade do ciclo completo (round-trip)**/

    @Nested
    @DisplayName("8 — Round-trip: gerar → parsear → validar")
    class RoundTrip {

        @Test
        @DisplayName("ciclo completo com UUID aleatório")
        void cicloCompletoComUuidAleatorio() {
            Usuario usuario = Usuario.builder()
                    .uuid(UUID.randomUUID())
                    .nomeCompleto("Max User")
                    .nomeUsuario("maxuser")
                    .email("max@gov.br")
                    .cpf("00000000000")
                    .senha("h")
                    .build();
            UserDetails ud = User.builder().username(usuario.getEmail()).password(usuario.getSenha()).build();

            String token = jwtService.gerarToken(usuario);
            UserDetails otherUserDetails = User.builder()
                    .username(usuario.getEmail())
                    .password(usuario.getSenha())
                    .build();
            assertThat(jwtService.isTokenValido(token, otherUserDetails)).isTrue();
            assertThat(jwtService.getUserLogin(token)).isEqualTo("max@gov.br");
        }

        @Test
        @DisplayName("ciclo completo com caracteres especiais no nome")
        void cicloCompletoComCaracteresEspeciais() {
            Usuario u = Usuario.builder()
                    .uuid(UUID.randomUUID())
                    .nomeCompleto("Álvaro Müller Ção")
                    .nomeUsuario("alvaro")
                    .email("alvaro@edu.br")
                    .cpf("55555555555")
                    .senha("h")
                    .build();
//            UserDetails ud = User.builder().username(u.getEmail()).password(u.getSenha()).build();

            String token  = jwtService.gerarToken(u);
            Claims claims = jwtService.obterClaims(token);
            assertThat(claims.get("nomeCompleto", String.class)).isEqualTo("Álvaro Müller Ção");
            
            UserDetails otherUserDetails = User.builder()
                    .username(usuarioBase.getEmail())
                    .password(usuarioBase.getSenha())
                    .build();
            assertThat(jwtService.isTokenValido(token, otherUserDetails)).isTrue();
        }

        @Test
        @DisplayName("getUserLogin deve ser consistente com subject retornado por obterClaims")
        void getUserLoginDeveSerConsistenteComObterClaims() {
            String token = jwtService.gerarToken(usuarioBase);
            String loginDireto = jwtService.getUserLogin(token);
            String loginViaClaims = jwtService.obterClaims(token).getSubject();
            assertThat(loginDireto).isEqualTo(loginViaClaims);
        }
    }

    /** MEUS HELPERS — fábrica de tokens bichado para os testes**/

    /** Token já expirado (expirou há 1 hora) */
    private String criarTokenExpirado() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        Instant agora = Instant.now();
        return Jwts.builder()
                .subject(usuarioBase.getEmail())
                .issuer(ISSUER)
                .issuedAt(Date.from(agora.minus(2, ChronoUnit.HOURS)))
                .expiration(Date.from(agora.minus(1, ChronoUnit.HOURS))) // expirou há 1h
                .signWith(key)
                .compact();
    }

    /** Token válido apenas por alguns segundos a partir de agora */
    private String criarTokenComExpiracaoEmSegundos(long segundos) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        Instant agora = Instant.now();
        return Jwts.builder()
                .subject(usuarioBase.getEmail())
                .issuer(ISSUER)
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plus(segundos, ChronoUnit.SECONDS)))
                .signWith(key)
                .compact();
    }

    /** Token assinado com uma chave diferente da configurada */
    private String criarTokenComChaveDiferente() {
        String chaveDiferente = "chave-completamente-diferente-e-invalida-256bits!!";
        SecretKey key = Keys.hmacShaKeyFor(chaveDiferente.getBytes(StandardCharsets.UTF_8));
        Instant agora = Instant.now();
        return Jwts.builder()
                .subject(usuarioBase.getEmail())
                .issuer(ISSUER)
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plus(60, ChronoUnit.MINUTES)))
                .signWith(key)
                .compact();
    }

    /** Token assinado com a chave correta, mas issuer diferente */
    private String criarTokenComIssuerDiferente() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        Instant agora = Instant.now();
        return Jwts.builder()
                .subject(usuarioBase.getEmail())
                .issuer("atacante-externo") // issuer inválido
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plus(60, ChronoUnit.MINUTES)))
                .signWith(key)
                .compact();
    }

    /** Token sem issuer algum */
    private String criarTokenSemIssuer() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        Instant agora = Instant.now();
        return Jwts.builder()
                .subject(usuarioBase.getEmail())
                // sem .issuer()
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plus(60, ChronoUnit.MINUTES)))
                .signWith(key)
                .compact();
    }

    /**
     * Simula o ataque "Algorithm None":
     * monta manualmente um JWT com header {"alg":"none"} e sem assinatura.
     * Formato: base64(header).base64(payload).  (sem assinatura)
     */
    private String criarTokenAlgNone() {
        // Gera um token real e extrai o payload (parte 2)
        String tokenReal = jwtService.gerarToken(usuarioBase);
        String payloadBase64 = tokenReal.split("\\.")[1];

        // Header declarando alg=none (Base64url de {"alg":"none","typ":"JWT"})
        String headerAlgNone = java.util.Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString("{\"alg\":\"none\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));

        return headerAlgNone + "." + payloadBase64 + "."; // sem assinatura
    }
    }

//1. Claims Padrão (Registered Claims)
//São informações recomendadas pela especificação do JWT para garantir a segurança do token. O objeto Claims possui métodos nativos para buscá-las:
//
//        claims.getSubject(): Retorna a identidade do usuário (geralmente o e-mail, username ou ID).
//
//        claims.getExpiration(): Retorna a data e hora em que o token expira.
//
//        claims.getIssuedAt(): Retorna o momento exato em que o token foi criado.
