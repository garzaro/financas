package com.cleber.financas.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.cleber.financas.api.common.GlobalExceptionHandler;
import com.cleber.financas.api.cookie.RefreshTokenCookieFactory;
import com.cleber.financas.api.dto.AutenticacaoResultado;
import com.cleber.financas.api.dto.RequestLoginDTO;
import com.cleber.financas.api.dto.TokenResponseDTO;
import com.cleber.financas.api.resource.UsuarioController;
import com.cleber.financas.exception.ErroResponse;
import com.cleber.financas.exception.TooManyRequestsException;
import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.model.repository.UsuarioRepository;
import com.cleber.financas.security.ratelimit.LimiteTentativasService;
import com.cleber.financas.security.ratelimit.LoginLimiteTentativasFilter;
import com.cleber.financas.service.JwtService;
import com.cleber.financas.service.UsuarioService;
import com.cleber.financas.service.impl.UsuarioServiceImpl;
import com.cleber.financas.service.token.RefreshTokenEmitido;
import com.cleber.financas.service.token.RefreshTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.HandlerExceptionResolver;

@ExtendWith(MockitoExtension.class)
class RateLimitTest {

    @Mock
    UsuarioRepository usuarioRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    LimiteTentativasService limiteTentativasService;
    @Mock
    Validator validator;
    @Mock
    AuthenticationManager authenticationManager;
    @Mock
    JwtService jwtService;
    @Mock
    StringRedisTemplate redisTemplate;
    @Mock
    RedisScript<Long> rateLimitScript;
    @Mock
    HandlerExceptionResolver resolver;
    @Mock
    HttpServletRequest request;
    @Mock
    HttpServletResponse response;
    @Mock
    FilterChain filterChain;

    @Test
    @DisplayName("bloqueio após N falhas: se bloqueado por email lança TooManyRequestsException com retryAfter em segundos")
    void bloqueadoPorEmailLancaTooManyRequests() {
        when(limiteTentativasService.isBloqueado(anyString())).thenReturn(true);
        when(limiteTentativasService.obterRetryAfter(anyString())).thenReturn(900L);

        UsuarioServiceImpl service = new UsuarioServiceImpl(usuarioRepository, passwordEncoder, validator, limiteTentativasService, null, authenticationManager, jwtService, 5, 2, 3);

        assertThatThrownBy(() -> service.autenticar(new RequestLoginDTO(" USER@TESTE.COM ", "senha"), "127.0.0.1", "JUnit"))
                .isInstanceOf(TooManyRequestsException.class)
                .satisfies(ex -> {
                    TooManyRequestsException tmre = (TooManyRequestsException) ex;
                    assertThat(tmre.getRetryAfterSeconds()).isEqualTo(900L);
                });

        verify(authenticationManager, never()).authenticate(any());
        verify(usuarioRepository, never()).findByEmail(anyString());
    }

    @Test
    @DisplayName("bloqueio após N falhas: conta mesmo que a conta não exista e normaliza e-mail com Locale.ROOT")
    void falhaContaMesmoQueContaNaoExista() {
        String email = " User@Teste.Com ";

        when(limiteTentativasService.isBloqueado(anyString())).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        UsuarioServiceImpl service = new UsuarioServiceImpl(usuarioRepository, passwordEncoder, validator, limiteTentativasService, null, authenticationManager, jwtService, 5, 2, 3);

        assertThatThrownBy(() -> service.autenticar(new RequestLoginDTO(email, "senha"), "127.0.0.1", "JUnit"))
                .isInstanceOf(BadCredentialsException.class);

        verify(limiteTentativasService).registrar(anyString(), eq(5L), eq(Duration.ofSeconds(2)), eq(Duration.ofSeconds(3)));
    }

    @Test
    @DisplayName("após N falhas no mesmo e-mail, a tentativa seguinte é bloqueada")
    void bloqueiaAposCincoFalhasNoMesmoEmail() {
        AtomicInteger falhas = new AtomicInteger();
        AtomicBoolean bloqueado = new AtomicBoolean();

        when(limiteTentativasService.isBloqueado(anyString())).thenAnswer(invocation -> bloqueado.get());
        when(limiteTentativasService.obterRetryAfter(anyString())).thenReturn(3L);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));
        doAnswer(invocation -> {
            if (falhas.incrementAndGet() >= 5) {
                bloqueado.set(true);
            }
            return null;
        }).when(limiteTentativasService).registrar(
                anyString(), eq(5L), eq(Duration.ofSeconds(2)), eq(Duration.ofSeconds(3)));

        UsuarioServiceImpl service = new UsuarioServiceImpl(usuarioRepository, passwordEncoder, validator,
                limiteTentativasService, null, authenticationManager, jwtService, 5, 2, 3);
        RequestLoginDTO request = new RequestLoginDTO(" USER@TESTE.COM ", "senha");

        for (int tentativa = 0; tentativa < 5; tentativa++) {
            assertThatThrownBy(() -> service.autenticar(request, "127.0.0.1", "JUnit"))
                    .isInstanceOf(BadCredentialsException.class);
        }

        assertThatThrownBy(() -> service.autenticar(request, "127.0.0.1", "JUnit"))
                .isInstanceOf(TooManyRequestsException.class)
                .satisfies(ex -> assertThat(((TooManyRequestsException) ex).getRetryAfterSeconds()).isEqualTo(3L));
        verify(authenticationManager, times(5)).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("bloqueio após N falhas: senha incorreta incrementa falha")
    void falhaSenhaIncorretaIncrementa() {
        String email = "user@teste.com";

        when(limiteTentativasService.isBloqueado(anyString())).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        UsuarioServiceImpl service = new UsuarioServiceImpl(usuarioRepository, passwordEncoder, validator, limiteTentativasService, null, authenticationManager, jwtService, 5, 2, 3);

        assertThatThrownBy(() -> service.autenticar(new RequestLoginDTO(email, "senhaErrada"), "127.0.0.1", "JUnit"))
                .isInstanceOf(BadCredentialsException.class);

        verify(limiteTentativasService).registrar(anyString(), eq(5L), eq(Duration.ofSeconds(2)), eq(Duration.ofSeconds(3)));
    }

    @Test
    @DisplayName("janela e limites configuráveis passados ao script Redis na classe única")
    void registrarExecutaScriptComJanelaELimites() {
        LimiteTentativasService service = new LimiteTentativasService(redisTemplate, rateLimitScript);
        String chave = "rl:login:fail:email:teste@email.com";

        service.registrar(chave, 5, Duration.ofSeconds(300), Duration.ofSeconds(900));

        verify(redisTemplate).execute(
                eq(rateLimitScript),
                anyList(),
                eq("5"),
                eq("300"),
                eq("900")
        );
    }

    @Test
    @DisplayName("zerar no sucesso: autenticação com sucesso limpa o contador e bloqueio e gera token")
    void sucessoZeraContador() {
        String email = "user@x.com";

        when(limiteTentativasService.isBloqueado(anyString())).thenReturn(false);
        Usuario u = Usuario.builder().email(email).senha("hash").build();
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(u));
        when(jwtService.gerarToken(u, List.of("USER"))).thenReturn("jwt-token-valido");
        RefreshTokenService refreshTokenService = mock(RefreshTokenService.class);
        when(refreshTokenService.gerar(any(), any(), any()))
                .thenReturn(new RefreshTokenEmitido("refresh-token", Duration.ofDays(7)));

        UsuarioServiceImpl service = new UsuarioServiceImpl(usuarioRepository, passwordEncoder, validator, limiteTentativasService, refreshTokenService, authenticationManager, jwtService, 5, 2, 3);
        AutenticacaoResultado res = service.autenticar(new RequestLoginDTO(email, "123"), "127.0.0.1", "JUnit");

        assertThat(res).isNotNull();
        assertThat(res.tokens().accessToken()).isEqualTo("jwt-token-valido");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(limiteTentativasService).limpar(anyString());
    }

    @Test
    @DisplayName("controller sem lógica de autenticação: delega POST /sign-in diretamente ao usuarioService.autenticar")
    void controllerDelegaAutenticacaoAoService() {
        UsuarioService mockUsuarioService = mock(UsuarioService.class);
        RefreshTokenCookieFactory cookieFactory = mock(RefreshTokenCookieFactory.class);
        TokenResponseDTO expectedResponse = new TokenResponseDTO("token-123");
        RefreshTokenEmitido refresh = new RefreshTokenEmitido("refresh-token", Duration.ofDays(7));
        AutenticacaoResultado expectedResult = new AutenticacaoResultado(expectedResponse, refresh);
        RequestLoginDTO requestDto = new RequestLoginDTO("user@test.com", "senha123");
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);

        when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(httpRequest.getHeader("User-Agent")).thenReturn("JUnit");
        when(cookieFactory.buildSet("refresh-token", Duration.ofDays(7)))
                .thenReturn(org.springframework.http.ResponseCookie
                        .from("refresh_token", "refresh-token").build());
        when(mockUsuarioService.autenticar(requestDto, "127.0.0.1", "JUnit"))
                .thenReturn(expectedResult);

        UsuarioController controller = new UsuarioController(
                null, mockUsuarioService, null, null, null, cookieFactory);
        ResponseEntity<TokenResponseDTO> responseEntity = controller.login(requestDto, httpRequest);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isEqualTo(expectedResponse);
        assertThat(responseEntity.getHeaders().getFirst(HttpHeaders.SET_COOKIE))
                .contains("refresh_token=refresh-token");
        verify(mockUsuarioService).autenticar(requestDto, "127.0.0.1", "JUnit");
    }

    @Test
    @DisplayName("zerar no sucesso: LimiteTentativasService.limpar remove chaves de count e block")
    void limparRemoveChavesCountEBlock() {
        LimiteTentativasService service = new LimiteTentativasService(redisTemplate, rateLimitScript);
        String chave = "rl:ip:127.0.0.1";

        service.limpar(chave);

        verify(redisTemplate).delete(anyList());
    }

    @Test
    @DisplayName("429 com Retry-After vindo do handler global (nunca 500) quando IP bloqueado")
    void filtroBloqueioIpRetorna429ComRetryAfterDelegandoAoResolver() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getServletPath()).thenReturn("/api/auth/sign-in");
        when(request.getRemoteAddr()).thenReturn("192.168.1.100");

        when(limiteTentativasService.isBloqueado(anyString())).thenReturn(true);
        when(limiteTentativasService.obterRetryAfter(anyString())).thenReturn(45L);

        LoginLimiteTentativasFilter filter = new LoginLimiteTentativasFilter(
                limiteTentativasService, resolver, 2, 3, 4);

        filter.doFilter(request, response, filterChain);

        verify(resolver).resolveException(
                eq(request),
                eq(response),
                isNull(),
                argThat(ex -> ex instanceof TooManyRequestsException && ((TooManyRequestsException) ex).getRetryAfterSeconds() == 45L)
        );
        verify(filterChain, never()).doFilter(any(), any());

        // Valida que o GlobalExceptionHandler transforma TooManyRequestsException em 429 com Retry-After header
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        TooManyRequestsException ex = new TooManyRequestsException("Muitas tentativas", 45L);
        ResponseEntity<ErroResponse> responseEntity = handler.handleTooManyRequests(ex);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(responseEntity.getHeaders().getFirst(HttpHeaders.RETRY_AFTER)).isEqualTo("45");
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getMensagem()).isEqualTo("Muitas tentativas");
    }

    @Test
    @DisplayName("volume por IP: requisições em sign-in e refresh incrementam a chave do IP")
    void volumePorIpIncrementaChaveEmSignInERefresh() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getServletPath()).thenReturn("/api/auth/refresh");
        when(request.getRemoteAddr()).thenReturn("10.0.0.1");

        when(limiteTentativasService.isBloqueado(anyString())).thenReturn(false);

        LoginLimiteTentativasFilter filter = new LoginLimiteTentativasFilter(
                limiteTentativasService, resolver, 2, 3, 4);

        filter.doFilter(request, response, filterChain);

        verify(limiteTentativasService).registrar(anyString(), eq(2L), eq(Duration.ofSeconds(3)), eq(Duration.ofSeconds(4)));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("passado o bloqueio, as requisições voltam a ser aceitas no filtro e no login")
    void passadoOBloqueioRequisicoesVoltamASerAceitas() throws Exception {
        AtomicBoolean bloqueado = new AtomicBoolean(true);

        when(limiteTentativasService.isBloqueado(anyString())).thenAnswer(inv -> bloqueado.get());
        when(limiteTentativasService.obterRetryAfter(anyString())).thenReturn(1L);

        LoginLimiteTentativasFilter filter = new LoginLimiteTentativasFilter(
                limiteTentativasService, resolver, 5, 1, 1);

        when(request.getMethod()).thenReturn("POST");
        when(request.getServletPath()).thenReturn("/api/auth/sign-in");
        when(request.getRemoteAddr()).thenReturn("10.0.0.2");

        // 1. Enquanto bloqueado -> filtra e gera TooManyRequestsException
        filter.doFilter(request, response, filterChain);
        verify(resolver).resolveException(eq(request), eq(response), isNull(), any(TooManyRequestsException.class));
        verify(filterChain, never()).doFilter(request, response);

        // 2. Passado o bloqueio (TTL expirou no Redis -> isBloqueado retorna false)
        bloqueado.set(false);
        filter.doFilter(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("passado o bloqueio por falhas de login, login volta a ser aceito")
    void passadoBloqueioLoginVoltaASerAceito() {
        AtomicBoolean bloqueado = new AtomicBoolean(true);
        String email = "user@teste.com";

        when(limiteTentativasService.isBloqueado(anyString())).thenAnswer(inv -> bloqueado.get());
        when(limiteTentativasService.obterRetryAfter(anyString())).thenReturn(1L);

        Usuario u = Usuario.builder().email(email).senha("hash").build();
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(u));
        when(jwtService.gerarToken(u, List.of("USER"))).thenReturn("jwt-pos-bloqueio");
        RefreshTokenService refreshTokenService = mock(RefreshTokenService.class);
        when(refreshTokenService.gerar(any(), any(), any()))
                .thenReturn(new RefreshTokenEmitido("refresh-token", Duration.ofDays(7)));

        UsuarioServiceImpl service = new UsuarioServiceImpl(usuarioRepository, passwordEncoder, validator,
                limiteTentativasService, refreshTokenService, authenticationManager, jwtService, 5, 1, 1);

        // 1. Enquanto bloqueado -> lança TooManyRequestsException
        assertThatThrownBy(() -> service.autenticar(new RequestLoginDTO(email, "senha"), "127.0.0.1", "JUnit"))
                .isInstanceOf(TooManyRequestsException.class);

        // 2. Passado o bloqueio
        bloqueado.set(false);
        AutenticacaoResultado response = service.autenticar(new RequestLoginDTO(email, "senha"), "127.0.0.1", "JUnit");
        assertThat(response).isNotNull();
        assertThat(response.tokens().accessToken()).isEqualTo("jwt-pos-bloqueio");
    }

    @Test
    @DisplayName("outras rotas e métodos não são limitados pelo filtro")
    void outrasRotasNaoSaoLimitadas() throws Exception {
        when(request.getMethod()).thenReturn("POST", "GET");
        when(request.getServletPath()).thenReturn("/api/auth/join/sign-up", "/api/auth/sign-in");

        LoginLimiteTentativasFilter filter = new LoginLimiteTentativasFilter(
                limiteTentativasService, resolver, 2, 3, 4);

        filter.doFilter(request, response, filterChain);
        filter.doFilter(request, response, filterChain);

        verifyNoInteractions(limiteTentativasService);
        verify(filterChain, times(2)).doFilter(request, response);
    }
}
