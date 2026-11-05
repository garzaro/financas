package com.cleber.financas.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.cleber.financas.api.dto.RefreshDadosToken;
import com.cleber.financas.exception.InvalidRefreshTokenException;
import com.cleber.financas.exception.RefreshTokenReuseException;
import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.model.repository.UsuarioRepository;
import com.cleber.financas.service.impl.tokenImpl.RefreshTokenServiceImpl;
import com.cleber.financas.service.token.RefreshTokenEmitido;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RefreshTokenService — geração, rotação, reuse e revogação")
class RefreshTokenServiceTest {

    private static final long TOKEN_TTL_MIN = 10_080L;
    private static final long GRACE_MS = 30_000L;

    @Mock
    private RedisTemplate<String, RefreshDadosToken> tokenTemplate;
    @Mock
    private StringRedisTemplate setTemplate;
    @Mock
    private ValueOperations<String, RefreshDadosToken> valueOps;
    @Mock
    private SetOperations<String, String> setOps;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private org.springframework.data.redis.core.script.RedisScript<Long> refreshRotationScript;

    private RefreshTokenServiceImpl service;
    private UUID usuarioId;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        service = new RefreshTokenServiceImpl(
                tokenTemplate, setTemplate, usuarioRepository, refreshRotationScript, TOKEN_TTL_MIN, GRACE_MS);
        usuarioId = UUID.randomUUID();
        usuario = Usuario.builder().uuid(usuarioId).email("cleber@gmail.com").build();

        lenient().when(tokenTemplate.opsForValue()).thenReturn(valueOps);
        lenient().when(setTemplate.opsForSet()).thenReturn(setOps);
    }

    @Test
    @DisplayName("gerar emite token bruto e persiste apenas o hash")
    void gerarEmiteTokenEPersisteHash() {
        RefreshTokenEmitido emitido = service.gerar(usuarioId, "127.0.0.1", "JUnit");

        assertThat(emitido.rawToken()).isNotBlank();
        assertThat(emitido.ttl()).isEqualTo(Duration.ofMinutes(TOKEN_TTL_MIN));

        verify(valueOps).set(startsWith("refresh:token:"), any(RefreshDadosToken.class),
                eq(Duration.ofMinutes(TOKEN_TTL_MIN)));
        verify(setOps).add(startsWith("refresh:family:"), anyString());
        verify(setOps).add(eq("refresh:user:" + usuarioId), anyString());
        verify(valueOps, never()).set(eq(emitido.rawToken()), any(), any());
    }

    @Test
    @DisplayName("rotacionar consome o token ativo, marca-o como rotacionado e emite um novo")
    void rotacionarEmiteNovoToken() {
        UUID familiaId = UUID.randomUUID();
        when(valueOps.get(anyString())).thenReturn(dados(RefreshDadosToken.Status.ATIVO, familiaId));
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS);
        com.fasterxml.jackson.databind.ObjectWriter writer = mapper.writerFor(RefreshDadosToken.class);
        String json = "{\"usuarioId\":\"" + usuarioId + "\",\"familiaId\":\"" + familiaId + "\",\"status\":\"ATIVO\",\"ip\":\"127.0.0.1\",\"agenteUsuario\":\"JUnit\",\"emitidoEm\":\"" + java.time.Instant.now().toString() + "\"}";
        try {
            RefreshDadosToken d = dados(RefreshDadosToken.Status.ATIVO, familiaId);
            json = writer.writeValueAsString(d);
        } catch (Exception e) {
        }
        org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer<RefreshDadosToken> ser =
                new org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer<>(mapper, RefreshDadosToken.class);
        lenient().doReturn(ser).when(tokenTemplate).getValueSerializer();
        when(tokenTemplate.execute(any(org.springframework.data.redis.core.script.RedisScript.class),
                any(java.util.List.class), any(Object[].class))).thenReturn(1L);

        RefreshTokenEmitido novo = service.rotacionar("token-bruto", "127.0.0.1", "JUnit");

        assertThat(novo.rawToken()).isNotBlank();
        assertThat(novo.rawToken()).isNotEqualTo("token-bruto");
    }

    @Test
    @DisplayName("rotacionar com token ausente lança InvalidRefreshTokenException com mensagem")
    void rotacionarTokenAusenteLancaInvalid() {
        when(valueOps.get(anyString())).thenReturn(null);

        assertThatThrownBy(() -> service.rotacionar("token-bruto", "127.0.0.1", "JUnit"))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessageContaining("não encontrado");
    }

    @Test
    @DisplayName("reuse de token rotacionado revoga a família e lança RefreshTokenReuseException")
    void reuseRevogaFamilia() {
        UUID familiaId = UUID.randomUUID();
        when(valueOps.get(anyString())).thenReturn(dados(RefreshDadosToken.Status.ATIVO, familiaId));
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS);
        org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer<RefreshDadosToken> ser =
                new org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer<>(mapper, RefreshDadosToken.class);
        lenient().doReturn(ser).when(tokenTemplate).getValueSerializer();
        when(tokenTemplate.execute(any(org.springframework.data.redis.core.script.RedisScript.class),
                any(java.util.List.class), any(Object[].class))).thenReturn(2L);

        assertThatThrownBy(() -> service.rotacionar("token-bruto", "127.0.0.1", "JUnit"))
                .isInstanceOf(RefreshTokenReuseException.class);
    }

    @Test
    @DisplayName("validar token ativo resolve o usuário")
    void validarTokenAtivoRetornaUsuario() {
        when(valueOps.get(anyString())).thenReturn(dados(RefreshDadosToken.Status.ATIVO, UUID.randomUUID()));
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));

        assertThat(service.validar("token-bruto")).isEqualTo(usuario);
    }

    @Test
    @DisplayName("validar token rotacionado lança InvalidRefreshTokenException")
    void validarTokenRotacionadoLancaInvalid() {
        when(valueOps.get(anyString()))
                .thenReturn(dados(RefreshDadosToken.Status.ROTACIONADO, UUID.randomUUID()));

        assertThatThrownBy(() -> service.validar("token-bruto"))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    @DisplayName("revogarAllByUser revoga os tokens de todas as famílias do usuário")
    void revogarAllByUserRevogaTodasAsFamilias() {
        UUID familiaId = UUID.randomUUID();
        when(setOps.members("refresh:user:" + usuarioId)).thenReturn(Set.of(familiaId.toString()));
        when(setOps.members("refresh:family:" + familiaId)).thenReturn(Set.of("hash-1"));

        service.revogarAllByUser(usuarioId);

        verify(tokenTemplate).delete("refresh:token:hash-1");
        verify(setTemplate).delete("refresh:family:" + familiaId);
        verify(setTemplate).delete("refresh:user:" + usuarioId);
    }

    @Test
    @DisplayName("rotacao concorrente com mesmo token: apenas um sucesso, outro e reuse")
    void rotacaoConcorrenteMesmoToken() throws Exception {
        UUID familiaId = UUID.randomUUID();
        when(valueOps.get(anyString())).thenReturn(dados(RefreshDadosToken.Status.ATIVO, familiaId));
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS);
        org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer<RefreshDadosToken> ser =
                new org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer<>(mapper, RefreshDadosToken.class);
        lenient().doReturn(ser).when(tokenTemplate).getValueSerializer();
        java.util.concurrent.atomic.AtomicInteger chamadas = new java.util.concurrent.atomic.AtomicInteger(0);
        when(tokenTemplate.execute(any(org.springframework.data.redis.core.script.RedisScript.class),
                any(java.util.List.class), any(Object[].class))).thenAnswer(invocation -> {
                    int c = chamadas.incrementAndGet();
                    if (c == 1) {
                        return 1L;
                    }
                    return 2L;
                });

        java.util.concurrent.CountDownLatch start = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.CountDownLatch done = new java.util.concurrent.CountDownLatch(2);
        java.util.concurrent.ExecutorService exec = java.util.concurrent.Executors.newFixedThreadPool(2);
        java.util.concurrent.atomic.AtomicReference<Object> r1 = new java.util.concurrent.atomic.AtomicReference<>();
        java.util.concurrent.atomic.AtomicReference<Object> r2 = new java.util.concurrent.atomic.AtomicReference<>();

        exec.submit(() -> {
            try {
                start.await();
                try {
                    r1.set(service.rotacionar("token-bruto", "127.0.0.1", "JUnit"));
                } catch (Exception e) {
                    r1.set(e);
                }
            } catch (Exception ignored) {
            } finally {
                done.countDown();
            }
        });
        exec.submit(() -> {
            try {
                start.await();
                try {
                    r2.set(service.rotacionar("token-bruto", "127.0.0.1", "JUnit"));
                } catch (Exception e) {
                    r2.set(e);
                }
            } catch (Exception ignored) {
            } finally {
                done.countDown();
            }
        });
        start.countDown();
        done.await(3, java.util.concurrent.TimeUnit.SECONDS);
        exec.shutdownNow();

        int sucessos = 0;
        int reuses = 0;
        if (r1.get() instanceof RefreshTokenEmitido) {
            sucessos++;
        } else if (r1.get() instanceof RefreshTokenReuseException) {
            reuses++;
        }
        if (r2.get() instanceof RefreshTokenEmitido) {
            sucessos++;
        } else if (r2.get() instanceof RefreshTokenReuseException) {
            reuses++;
        }
        assertThat(sucessos).isEqualTo(1);
        assertThat(reuses).isEqualTo(1);
    }

    private RefreshDadosToken dados(RefreshDadosToken.Status status, UUID familiaId) {
        return new RefreshDadosToken(usuarioId, familiaId, status, "127.0.0.1", "JUnit", Instant.now());
    }
}
