package com.cleber.financas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Todo-list
 * [x] verificar usuario ativo ao ser criado na base
 * [] criar endpoint de login no front, que vai retornar o accesstoken JWT
 * [x] testar no front com usuario e senha qualquer, não existe esse usuario na base
 * [x] ver se a porta do vite é a 3000
 * [] atulizando apagina logada retorna pra pagina de login
 * @EnableConfigurationProperties - se falhar a aplicação nem sobe,
 * melhor maneira de configurar propriedades do projeto
 *
 * Resumo da estrutura atual:
 *
 * Hospedagem aplicaçao:
 * Render - rodando o container Docker com o Spring Boot.
 * Cloudflare - rodando o react.
 *
 * Banco de Dados Relacional: Neon (PostgreSQL externo).
 *
 * Cache / Sessão: Upstash (Redis externo).
 *
 * Fiz a leitura de toda a camada de segurança, do fluxo de tokens e dos testes. Segue a análise orientada à aceitação de pronto, com veredito por critério e os bloqueadores que impedem o aceite hoje.
 * Análise da camada de segurança — tokens
 * Escopo avaliado: geração, expiração, refresh após expiração do access, revogação e emissão ao cliente.
 * Método: análise estática do código em src/main/java, configuração (application.properties/.env) e testes.
 * Veredito geral
 * NÃO PRONTO para aceitação. O desenho é correto e maduro (rotação com detecção de reuso, hash no servidor, cookies HttpOnly), mas há 1 bloqueador funcional grave (o filtro JWT não autentica), falhas de correção no tratamento de erro/atomicidade e ausência total de testes executáveis. No caminho feliz o fluxo “parece” funcionar, mas não há evidência de aceite e partes centrais estão desligadas.
 *
 * Placar por critério
 * #	Critério de aceitação	Status	Resumo
 *
 * 1 [x]	Geração de tokens	⚠️	Access e refresh são gerados corretamente;
 *
 * 2 []	Expiração	❌	TTLs corretos, mas o access token não é validado em requisições
 *
 * 3 []	Refresh após expirar o access	⚠️	Lógica existe e é permitAll, mas o token novo não serve (filtro)
 *
 * 4 []	Revogação	⚠️/❌	Revoga refresh (todas as sessões), mas não revoga access
 *
 * 5 [x]	Emissão ao cliente	✅	Access no body, refresh em cookie HttpOnly/Secure/SameSite
 *
 * 6 []	Evidência automatizada	❌	Testes não compilam e o de refresh está vazio
 *
 *
 * 1. Geração — ⚠️
 *
 * Access token — JwtServiceImpl.gerarToken (service/impl/JwtServiceImpl.java:44-70):
 *
 * [x] ✅ Assina HS512, issuer="financas-api", subject=email, claims uuid/nomeCompleto/cpf/horaExpiracao.
 *
 * [x] ✅ Expiração calculada em localHouse.now().plusMinutes(exp); com JWT_EXPIRATION=30 (.env:29) → 30 min. Correto na prática.
 *
 * ⚠️ [x] Usa API depreciada do jjwt 0.12 (SignatureAlgorithm.HS512 + signWith(alg, String)). Recomenda-se signWith(secretKey, Jwts.SIG.HS512).
 *
 * ⚠️ [] O nome jwtExpiration é enganoso: o código trata o valor como minutos, mas o README documenta 3600000 (ms). Se alguém seguir o README, o
 * token passa a “expirar” em ~6,8 anos. Armadilha de configuração.
 *
 * ⚠️ [] Sem jti → impossibilita revogar o access individualmente (ver item 4).
 *
 * ⚠️ [x] Payload contém CPF/email (PII) legível via Base64 (JWT não é cifrado).
 *
 *
 * Refresh token — RefreshTokenServiceImpl (service/impl/tokenImpl/RefreshTokenServiceImpl.java):
 *
 * [x] ✅ 256 bits de entropia (KeyGenerators.secureRandom(32), tokenAleatorio() linha 170).
 *
 * [x] ✅ Persiste apenas o hash SHA-256 (hash(), linha 176) — proteção contra vazamento do store.
 *
 * [x] ✅ Família (refresh:family:*) + índice por usuário (refresh:user:*), TTL de 7 dias (JWT_REFRESH_EXPIRATION=604800000).
 *
 * ⚠️ [] A lógica de rotação não é atômica (get e depois set, linhas 70–86; o .getAndDelete() está comentado). Sob concorrência, duas requisições
 * podem ler ATIVO e emitir dois tokens válidos da mesma família, e a detecção de reuso pode não disparar.
 *
 * ⚠️ [] rotacionar não vincula IP/User-Agent ao token (um token roubado rotaciona de qualquer lugar).
 *
 * ⚠️ [] Artefatos órfãos: entidade JPA RefreshToken, enum TokenStatus e RefreshTokenStatus não são usados pelo store (que é 100% Redis) e não existe RefreshTokenRepository. Confunde manutenção.
 *
 *
 * 2. Expiração — ❌
 *
 * [x] ✅ Access: 30 min (efetivo). Refresh: 7 dias (Redis TTL + cookie maxAge).
 *
 * ❌ Bloqueador B1: JwtAuthenticationFilter.doFilterInternal tem to-do o corpo de autenticacao comentado (security/JwtAuthenticationFilter.java:79-118). O filtro apenas chama filterChain.doFilter (linha 120) e nunca popula o SecurityContext. Com .anyRequest().authenticated() (config/SecurityConfig.java:98), os endpoints protegidos retornam 403 e o access token não é validado em nenhuma requisição. Ou seja: a expiração do access token é irrelevante, porque ele nunca é conferido. Isso foi introduzido no commit 59e343e (“refatore JwtAuthenticationFilter…”) — é uma regressão.
 *
 * ⚠️ [] isTokenValido (JwtServiceImpl.java:88-105) só captura ExpiredJwtException; MalformedJwtException, assinatura inválida etc. propagam. Também faz verificação manual de data redundante (o parse já valida exp) e não confere o subject do token contra o usuário.
 *
 *
 * 3. Refresh após expiração do access — ⚠️
 *
 * [x] ✅ POST /api/auth/refresh está permitAll e isento do filtro (SecurityConfig.java:90, JwtAuthenticationFilter.java:52), lê o cookie, rotaciona e emite novo par (UsuarioController.refresh, linhas 130-159). O desenho atende “renovar após expirar”.
 *
 * ❌ Na prática, após renovar, o novo access token não é aceito nos endpoints protegidos (B1). O fluxo não se fecha.
 *
 * ⚠️ [] Em refresh chama-se rotacionar(...) e depois validar(emitido.rawToken()) (linhas 142–146) — uma segunda ida ao Redis desnecessária;
 * rotacionar já poderia devolver o usuário.
 *
 * ⚠️ [] Detecção de reuso retorna o código errado: em rotacionar (linha 77) lança InvalidRefreshTokenException (→ 401), então o handler de RefreshTokenReuseException (→ 403, GlobalExceptionHandler.java:41-45) nunca é acionado. A classe RefreshTokenReuseException está morta.
 *
 * ⚠️ [] Bug de mensagem: InvalidRefreshTokenException não chama super(message) (exception/InvalidRefreshTokenException.java:4-5) → o 401 responde {"message":null} em todos os casos (inclui token ausente/expirado).
 *
 *
 * 4. Revogação — ⚠️/❌
 *
 * [x] ✅ Reuso detectado → revoga a família inteira (revogarFamiliaById, linha 153).
 *
 * [x] ✅ logout → revogarAllByUser (UsuarioController.java:165) apaga tokens de todas as famílias do usuário e limpa o cookie.
 *
 * ⚠️ [] O logout faz “todos os dispositivos”, mas o comentário/doc fala em logoff de um dispositivo (RefreshTokenService.java:28-32). Existe revogarFamilia no contrato, mas não é exposta em nenhum endpoint — não há logout por dispositivo.
 *
 * ❌ Access token não pode ser revogado: sem jti/denylist, um token roubado continua válido por até 30 min após o logout. Para “revogação imediata” como critério, isso reprova.
 *
 * ⚠️ [] POST /api/auth/logout exige autenticação (não está em shouldNotFilter). Como o filtro está desligado (B1), o logout retorna 403 sempre. Além disso, se o refresh já expirou, validar lança exceção (401) e o cookie não é limpo.
 *
 * ⚠️ [] O set refresh:user:{id} não tem TTL (linha 148) — pode crescer indefinidamente se o usuário nunca fizer logout.
 *
 *
 * 5. Emissão ao cliente — ✅ (com ressalva)
 *
 * [x] ✅ Access token no corpo JSON (TokenResponseDTO, tokenType="Bearer"); refresh token em cookie refresh_token com HttpOnly, Secure (configurável), SameSite=Strict, Path=/api/auth, Max-Age=ttl (RefreshTokenCookieFactory).
 *
 * [x] ✅ CORS com allowCredentials=true e origens explícitas (CORS_ALLOWED_ORIGINS), sem wildcard — coerente com cookie.
 *
 * ⚠️ [] SameSite=Strict + SPA cross-site: se o front (www.mepoupepessoal.com) e a API estiverem em domínios diferentes, o navegador não enviará o cookie no /refresh a partir de uma chamada XHR cross-site. Cenário típico exige SameSite=None; Secure (e, aí, proteção CSRF). Validar o deployment antes de aceitar.
 *
 * ⚠️ [] Sem rate limiting/lockout no /sign-in e /join/sign-up (já apontado em notes.md).
 *
 *
 * 6. Evidência automatizada — ❌
 *
 * [] src/test/.../RefreshTokenServiceTest.java está vazio.
 *
 * [] JwtServiceTest.java não compila: referencia gerarToken(UserDetails), extrairUsernameToken, obterClaims(Map), isTokenValido(String, UserDetails) — nenhum existe no JwtService atual. Isso quebra o mvn test inteiro (falha em test-compile), logo não há suíte de aceite.
 *
 * [] application-test.properties define jwtExpirationMs (nome que o código não lê), não define spring.app.refreshRotatedGraceMs (exigido sem default pelo RefreshTokenServiceImpl) e não configura Redis — o contexto de teste não sobe com o bean de refresh.
 *
 * [] Não há testcontainers-redis.
 *
 *
 * Bloqueadores de aceitação (ordem de prioridade)
 *
 * ID	Bloqueador	Local
 * [] B1	Filtro JWT não autentica (corpo comentado) → access token nunca validado; endpoints protegidos e o próprio logout dão 403	security/JwtAuthenticationFilter.java:79-120
 * [] B2	Suíte de testes não compila + teste de refresh vazio → sem evidência de aceite	test/.../JwtServiceTest.java, RefreshTokenServiceTest.java
 * [] B3	InvalidRefreshTokenException perde a mensagem (super) → 401 com message:null	exception/InvalidRefreshTokenException.java:4
 * [] B4	Reuso lança InvalidRefreshTokenException (401) em vez de RefreshTokenReuseException (403); handler 403 morto	RefreshTokenServiceImpl.java:77
 * [] B5	Rotação não atômica (get+set) → risco de 2 tokens válidos / reuso não detectado sob concorrência	RefreshTokenServiceImpl.java:70-88
 * [] B6	Logout revoga só refresh e é “todos os dispositivos”; access permanece válido (sem jti/denylist)	UsuarioController.java:161-172
 * [] B7	ddl-auto=create em produção (destrutivo) — risco de perda de dados	application.properties:14
 *
 *Riscos / dívidas menores
 *
 *[] API depreciada (SignatureAlgorithm.HS512); nome/README de jwtExpiration induz a erro de unidade (min vs ms).
 *
 * [] SameSite=Strict pode inviabilizar cookie cross-site no /refresh.
 *
 * [] Sem rate limiting/lockout no login/cadastro.
 *
 * [] Artefatos mortos/duplicados: entidade RefreshToken, TokenStatus, RefreshTokenStatus, RefreshTokenReuseException, UsuarioAutenticado (id Long, incompatível com UUID).
 *
 * [] CORS com setAllowCredentials(true) duplicado (inofensivo).
 *
 * Plano de aceitação sugerido
 *
 * [] Corrigir B1 (restaurar validação/autenticação no filtro: extrair subject, loadUserByUsername, setar SecurityContext, tratar Expired/Malformed).
 *
 * [] Corrigir B3/B4 (mensagem + exceção de reuso correta e handler 403).
 *
 * [] Tornar a rotação atômica (Lua/WATCH/lock distribuído) e definir política de logout (por dispositivo vs. todas).
 *
 * [] Decidir estratégia de revogação de access (jti + denylist no Redis com TTL = exp) — necessária para o critério “revogação”.
 * [] Alinhar properties de teste (refreshRotatedGraceMs, Redis/Testcontainers) e reescrever os testes: geração, expiração, refresh pós-expiração, rotação, reuso (403 + família revogada), logout, e ausência de token.
 * ddl-auto=validate/none; documentar unidade de JWT_EXPIRATION.
 * [] Validar SameSite/CORS conforme domínio real do front.
 * [] Um detalhe importante de processo: como reparos 1–7 alteram código, a implementação precisa ser feita em outro agente (não estou autorizado a modificar arquivos em Plan mode).
 *
 * **/

@EnableScheduling
@EnableConfigurationProperties
@SpringBootApplication
public class FinancasApplication implements WebMvcConfigurer {

	public static void main(String[] args) {

		SpringApplication.run(FinancasApplication.class, args);
		}
	}


