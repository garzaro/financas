package com.cleber.financas.service.impl;

import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes completos do JwtServiceImpl cobrindo:
 *  - Geração de token
 *  - Validação de claims
 *  - Expiração
 *  - Tokens adulterados / assinatura inválida
 *  - Token sem subject
 *  - getUserLogin
 *  - isTokenValido (todos os ramos)
 *  - obterClaims (todos os ramos)
 *  - Proteção contra Alg None
 *  - Validação do ISSUER
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "application.security.jwt.secret-key=chave-super-secreta-de-testes-com-256bits-minimos-ok",
    "application.security.jwt.expiration=60",
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false"
})
@DisplayName("JwtService - Testes de seguranca completos")
class JwtServiceTest {

    /* constantes reutilizadas */
    private static final String SECRET =
            "chave-super-secreta-de-testes-com-256bits-minimos-ok";

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
    private static final String ISSUER = "financas";

    @Autowired
    private JwtService jwtService;

    private Usuario usuarioBase;

    /**configuration**/ 
    @BeforeEach
    void setUp() {
        usuarioBase = Usuario.builder()
                .id(1L)
                .nomeCompleto("Cleber Teste")
                .nomeUsuario("cleber")
                .email("cleber@gmail.com")
                .cpf("12345678900")
                .senha("senha-hash-qualquer")
                .build();
    }

    /**1- geracao do token*/ 
    @Nested
    @DisplayName("geracao do token")
    class GerarToken {

        @Test
        @DisplayName("deve retornar uma string JWT não nula e não vazia")
        void deveRetornarTokenNaoNuloNaoVazio() {
            String token = jwtService.gerarToken(usuarioBase);
            assertThat(token).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("deve gerar token com 3 partes separadas por ponto (header.payload.signature)")
        void deveGerarTokenComTresPartesHeaderPayloadSignature() {
            String token = jwtService.gerarToken(usuarioBase);
            String[] partes = token.split("\\.");
            assertThat(partes).hasSize(3);
        }

        @Test
        @DisplayName("dois tokens gerados para o mesmo usuário devem ser diferentes (timestamp diferente)")
        void doisTokensDevemTerTimestampDiferentes() throws InterruptedException {
            String token1 = jwtService.gerarToken(usuarioBase);
            Thread.sleep(1_100); // garante issuedAt diferente (precisão em segundos)
            String token2 = jwtService.gerarToken(usuarioBase);
            assertThat(token1).isNotEqualTo(token2);
        }
    }

    /**2. CLAIMS**/
    @Nested
    @DisplayName("obterClaims()")
    class ObterClaims {

        @Test
        @DisplayName("subject deve ser o email do usuário")
        void subjectDeveSerEmail() {
            String token = jwtService.gerarToken(usuarioBase);
            //decodificando a carga útil (o Payload) do JWT e extraindo um mapa de chaves e valores, cada chaves e valores é um claim
            Claims claims = jwtService.obterClaims(token); //Retorna a identidade do usuário - email, cpf
            assertThat(claims.getSubject()).isEqualTo("cleber@gmail.com");
        }

        @Test
        @DisplayName("claim 'id' deve corresponder ao id do usuário")
        void claimIdDeveEstarPresenteCorreponderUsuario() {
            String token = jwtService.gerarToken(usuarioBase);
            Claims claims = jwtService.obterClaims(token);
            // JJWT deserializa números como Integer ou Long dependendo do valor
            Number idClaim = (Number) claims.get("id");
            assertThat(idClaim.longValue()).isEqualTo(1L);
        }

        @Test
        @DisplayName("claim 'cpf' deve estar presente e correto")
        void claimCpfDeveEstarPresente() {
            String token = jwtService.gerarToken(usuarioBase);
            Claims claims = jwtService.obterClaims(token);
            assertThat(claims.get("cpf", String.class)).isEqualTo("12345678900");
        }

        @Test
        @DisplayName("claim 'nome_usuario' deve estar presente e correto")
        void claimNomeUsuarioDeveEstarPresente() {
            String token = jwtService.gerarToken(usuarioBase);
            Claims claims = jwtService.obterClaims(token);
            assertThat(claims.get("nome_usuario", String.class)).isEqualTo("cleber");
        }

        @Test
        @DisplayName("claim 'nome' deve estar presente e correto")
        void claimNomeDeveEstarPresente() {
            String token = jwtService.gerarToken(usuarioBase);
            Claims claims = jwtService.obterClaims(token);
            assertThat(claims.get("nome", String.class)).isEqualTo("Cleber Teste");
        }

        @Test
        @DisplayName("claim 'horaExpiracao' deve estar presente e não vazio")
        void claimHoraExpiracaoDeveEstarPresenteNaoVazio() {
            String token = jwtService.gerarToken(usuarioBase);
            Claims claims = jwtService.obterClaims(token);
            assertThat(claims.get("horaExpiracao", String.class))
                    .isNotNull()
                    .isNotBlank()
                    .matches("\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}:\\d{2}");
        }

        @Test
        @DisplayName("issuer deve ser 'financas'")
        void issuerDeveSerFinancas() {
            String token = jwtService.gerarToken(usuarioBase);
            Claims claims = jwtService.obterClaims(token);
            assertThat(claims.getIssuer()).isEqualTo(ISSUER);
        }

        @Test
        @DisplayName("issuedAt deve ser anterior ou igual ao momento atual")
        void emissaoIssuedAtDeveSerAnteriorAoMomentoAtual() {
            String token = jwtService.gerarToken(usuarioBase);
            Claims claims = jwtService.obterClaims(token);
            assertThat(claims.getIssuedAt()).isBeforeOrEqualTo(new Date());
        }

        @Test
        @DisplayName("expiration deve ser posterior ao issuedAt")
        void expirationDeveSerPosteriorAoIssuedAt() {
            String token = jwtService.gerarToken(usuarioBase);
            Claims claims = jwtService.obterClaims(token);
            assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
        }

        @Test
        @DisplayName("expiration deve ser posterior ao momento atual")
        void expirationDeveSerPosteriorAoMomentoAtual() {
            String token = jwtService.gerarToken(usuarioBase);
            Claims claims = jwtService.obterClaims(token);
            assertThat(claims.getExpiration()).isAfter(new Date());
        }
    }

    /**3. getUserLogin**/

    @Nested
    @DisplayName("3 — getUserLogin()")
    class GetUserLogin {

        @Test
        @DisplayName("deve retornar o email do usuário dono do token")
        void deveRetornarEmailDoDonoDoTokenCorretamente() {
            String token = jwtService.gerarToken(usuarioBase);
            String login = jwtService.getUserLogin(token);
            assertThat(login).isEqualTo("cleber@gmail.com");
        }

        @Test
        @DisplayName("deve diferenciar tokens de usuários distintos")
        void deveDiferenciarTokensDeUsuariosDistintos() {
            /**cenario**/
            Usuario usuarioDistinto = Usuario.builder()
                    .id(2L)
                    .nomeCompleto("Usuario Distinto")
                    .nomeUsuario("distinto")
                    .email("distinto@gmail.com")
                    .cpf("98765432100")
                    .senha("hash-distinta")
                    .build();
            /**acção/execução**/
            String tokenA = jwtService.gerarToken(usuarioBase);
            String tokenB = jwtService.gerarToken(usuarioDistinto);
            /**verificação**/
            assertThat(jwtService.getUserLogin(tokenA)).isEqualTo("cleber@gmail.com");
            assertThat(jwtService.getUserLogin(tokenB)).isEqualTo("distinto@gmail.com");
            assertThat(jwtService.getUserLogin(tokenA)).isNotEqualTo("distinto@gmail.com");
            assertThat(jwtService.getUserLogin(tokenB)).isNotEqualTo("cleber@gmail.com");
        }
    }

    /**4. isTokenValido — caminho feliz**/

    @Nested
    @DisplayName("isTokenValido() — token válido")
    class IsTokenValidoFeliz {

        @Test
        @DisplayName("token recém-gerado deve ser válido")
        void tokenRecenteDeveSerValido() {
            String token = jwtService.gerarToken(usuarioBase);
            assertThat(jwtService.isTokenValido(token)).isTrue();
        }

        @Test
        @DisplayName("token válido de usuário diferente também deve retornar true")
        void tokenDeOutroUsuarioDeveSerValido() {
            Usuario outro = Usuario.builder()
                    .id(99L)
                    .nomeCompleto("usuario")
                    .nomeUsuario("usuario")
                    .email("usuario@gmail.com")
                    .cpf("12345678900")
                    .senha("senha")
                    .build();
            assertThat(jwtService.isTokenValido(jwtService.gerarToken(outro))).isTrue();
        }
    }

    /**5. isTokenValido — cenários de falha / segurança**/

    @Nested
    @DisplayName("5 — isTokenValido() — cenários de falha")
    class IsTokenValidoFalha {

        @Test
        @DisplayName("token expirado deve retornar false")
        void tokenExpiradoDeveRetornarFalse() {
            // cria token já expirado manualmente
            String tokenExpirado = criarTokenExpirado();
            assertThat(jwtService.isTokenValido(tokenExpirado)).isFalse();
        }

        @Test
        @DisplayName("token com assinatura adulterada deve retornar false")
        void tokenComAssinaturaAdulteradaDeveRetornarFalse() {
            String token = jwtService.gerarToken(usuarioBase);
            // corrompe o último caractere da assinatura
            String adulterado = token.substring(0, token.length() - 3) + "QUEBROU-LA-DENTRO";
            assertThat(jwtService.isTokenValido(adulterado)).isFalse();
        }

        @Test
        @DisplayName("token completamente inválido deve retornar false")
        void tokenInvalidoDeveRetornarFalse() {
            assertThat(jwtService.isTokenValido("isso.nao.e.um.token")).isFalse();
        }

        @Test
        @DisplayName("token vazio deve retornar false")
        void tokenVazioDeveRetornarFalse() {
            assertThat(jwtService.isTokenValido("")).isFalse();
        }

        @Test
        @DisplayName("token null deve retornar false")
        void tokenNullDeveRetornarFalse() {
            assertThat(jwtService.isTokenValido(null)).isFalse();
        }

        @Test
        @DisplayName("token assinado com chave diferente deve retornar false")
        void tokenAssinadoComChaveDiferenteDeveRetornarFalse() {
            String tokenFalso = criarTokenComChaveDiferente();
            assertThat(jwtService.isTokenValido(tokenFalso)).isFalse();
        }

        @Test
        @DisplayName("token com issuer diferente deve retornar false")
        void tokenComIssuerDiferenteDeveRetornarFalse() {
            String tokenFalso = criarTokenComIssuerDiferente();
            assertThat(jwtService.isTokenValido(tokenFalso)).isFalse();
        }

        @Test
        @DisplayName("token sem issuer deve retornar false (ISSUER é obrigatório)")
        void tokenSemIssuerDeveRetornarFalse() {
            String tokenSemIssuer = criarTokenSemIssuer();
            assertThat(jwtService.isTokenValido(tokenSemIssuer)).isFalse();
        }
    }

    /**6. obterClaims — cenários de exceção**/

    @Nested
    @DisplayName("obterClaims() — cenários de exceção")
    class ObterClaimsExcecoes {

        @Test
        @DisplayName("token expirado deve lançar ExpiredJwtException")
        void tokenExpiradoDeveLancarExpiredJwtException() {
            String tokenExpirado = criarTokenExpirado();
            assertThatThrownBy(() -> jwtService.obterClaims(tokenExpirado))
                    .isInstanceOf(ExpiredJwtException.class);
        }

        @Test
        @DisplayName("token adulterado deve lançar RuntimeException")
        void tokenAdulteradoDeveLancarRuntimeException() {
            String token = jwtService.gerarToken(usuarioBase);
            String adulterado = token.substring(0, token.length() - 3) + "ZZZ";
            assertThatThrownBy(() -> jwtService.obterClaims(adulterado))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Token JWT inválido");
        }

        @Test
        @DisplayName("token malformado deve lançar RuntimeException com mensagem esperada")
        void tokenMalformadoDeveLancarRuntimeException() {
            assertThatThrownBy(() -> jwtService.obterClaims("lixo.total.aqui"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Token JWT inválido");
        }

        @Test
        @DisplayName("token assinado com outra chave deve lançar RuntimeException")
        void tokenDeOutraChaveDeveLancarRuntimeException() {
            String tokenFalso = criarTokenComChaveDiferente();
            assertThatThrownBy(() -> jwtService.obterClaims(tokenFalso))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Token JWT inválido");
        }

        @Test
        @DisplayName("token com issuer errado deve lançar RuntimeException")
        void tokenComIssuerErradoDeveLancarRuntimeException() {
            String tokenFalso = criarTokenComIssuerDiferente();
            assertThatThrownBy(() -> jwtService.obterClaims(tokenFalso))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Token JWT inválido");
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // 7. Proteção contra Alg None (ataque crítico de JWT)
    // ════════════════════════════════════════════════════════════════════════

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
            String tokenAlgNone = criarTokenAlgNone();
            assertThat(jwtService.isTokenValido(tokenAlgNone)).isFalse();
        }

        @Test
        @DisplayName("token com alg=none deve lançar RuntimeException em obterClaims")
        void tokenAlgNoneDeveLancarExcecaoEmObterClaims() {
            String tokenAlgNone = criarTokenAlgNone();
            assertThatThrownBy(() -> jwtService.obterClaims(tokenAlgNone))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // 8. Integridade do ciclo completo (round-trip)
    // ════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("8 — Round-trip: gerar → parsear → validar")
    class RoundTrip {

        @Test
        @DisplayName("ciclo completo com usuário de id máximo (Long)")
        void cicloCompletoComIdMaximo() {
            Usuario u = Usuario.builder()
                    .id(Long.MAX_VALUE)
                    .nomeCompleto("Max User")
                    .nomeUsuario("maxuser")
                    .email("max@gov.br")
                    .cpf("00000000000")
                    .senha("h")
                    .build();

            String token = jwtService.gerarToken(u);
            assertThat(jwtService.isTokenValido(token)).isTrue();
            assertThat(jwtService.getUserLogin(token)).isEqualTo("max@gov.br");

            Claims claims = jwtService.obterClaims(token);
            assertThat(((Number) claims.get("id")).longValue()).isEqualTo(Long.MAX_VALUE);
        }

        @Test
        @DisplayName("ciclo completo com caracteres especiais no nome")
        void cicloCompletoComCaracteresEspeciais() {
            Usuario u = Usuario.builder()
                    .id(5L)
                    .nomeCompleto("Álvaro Müller Ção")
                    .nomeUsuario("alvaro")
                    .email("alvaro@edu.br")
                    .cpf("55555555555")
                    .senha("h")
                    .build();

            String token = jwtService.gerarToken(u);
            Claims claims = jwtService.obterClaims(token);
            assertThat(claims.get("nome", String.class)).isEqualTo("Álvaro Müller Ção");
            assertThat(jwtService.isTokenValido(token)).isTrue();
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
