package com.cleber.financas.model.repository;

import com.cleber.financas.model.entity.RefreshToken;
import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.model.enums.TokenStatus;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
public class RefreshTokenRepositoryIntegrationTest {

    @Container
    public static GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379).toString());
    }

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario createAndSaveUsuario(String suffix) {
        Usuario usuario = Usuario.builder()
            .nomeCompleto("Usuario Teste " + suffix)
            .cpf("123456" + suffix)
            .nomeUsuario("usuario.teste." + suffix)
            .email("test." + suffix + "@test.com")
            .senha("senha-segura")
            .isAtivo(true)
            .build();
        return usuarioRepository.save(usuario);
    }

    private RefreshToken createAndSaveToken(Usuario usuario, String tokenHash, TokenStatus status) {
        RefreshToken token = RefreshToken.builder()
                .usuario(usuario)
                .tokenHash(tokenHash)
                .status(status)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        return refreshTokenRepository.save(token);
    }

    @Test
    void testSaveAndFind_ShouldPersistInRedis() {
        // Arrange
        Usuario usuario = createAndSaveUsuario("00001");
        String tokenHash = "dummyhash123";

        RefreshToken token = RefreshToken.builder()
            .usuario(usuario)
            .tokenHash(tokenHash)
            .status(TokenStatus.ACTIVE)
            .createdAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            .build();

        // Act
        refreshTokenRepository.save(token);
        Optional<RefreshToken> retrievedOpt = refreshTokenRepository.findByTokenHash(tokenHash);

        // Assert
        assertTrue(retrievedOpt.isPresent());
        RefreshToken retrieved = retrievedOpt.get();
        assertEquals(usuario.getUuid(), retrieved.getUsuario().getUuid());
        assertEquals(TokenStatus.ACTIVE, retrieved.getStatus());

        // Verifica findAllByUsuarioUuidAndStatus
        List<RefreshToken> userTokens = refreshTokenRepository.findAllByUsuarioUuidAndStatus(usuario.getUuid(), TokenStatus.ACTIVE);
        assertEquals(1, userTokens.size());
    }

    @Test
    void testFindAllByUsuarioUuid_ShouldReturnOnlyUserTokens() {
        Usuario usuarioA = createAndSaveUsuario("00002");
        Usuario usuarioB = createAndSaveUsuario("00003");

        createAndSaveToken(usuarioA, "hash-user-a-1", TokenStatus.ACTIVE);
        createAndSaveToken(usuarioA, "hash-user-a-2", TokenStatus.REVOKED);
        createAndSaveToken(usuarioB, "hash-user-b-1", TokenStatus.ACTIVE);

        List<RefreshToken> userATokens = refreshTokenRepository.findAllByUsuarioUuid(usuarioA.getUuid());

        assertEquals(2, userATokens.size());
        assertTrue(userATokens.stream().allMatch(token -> token.getUsuario().getUuid().equals(usuarioA.getUuid())));
    }

    @Test
    void testFindAllByStatus_ShouldReturnOnlyMatchingStatus() {
        Usuario usuarioA = createAndSaveUsuario("00004");
        Usuario usuarioB = createAndSaveUsuario("00005");

        String activeHash1 = "hash-status-active-1";
        String activeHash2 = "hash-status-active-2";

        createAndSaveToken(usuarioA, activeHash1, TokenStatus.ACTIVE);
        createAndSaveToken(usuarioB, activeHash2, TokenStatus.ACTIVE);
        createAndSaveToken(usuarioA, "hash-status-revoked-1", TokenStatus.REVOKED);

        List<RefreshToken> activeTokens = refreshTokenRepository.findAllByStatus(TokenStatus.ACTIVE);
        Set<String> activeHashes = activeTokens.stream()
                .map(RefreshToken::getTokenHash)
                .collect(Collectors.toSet());

        assertTrue(activeHashes.contains(activeHash1));
        assertTrue(activeHashes.contains(activeHash2));
        assertTrue(activeTokens.stream().allMatch(token -> token.getStatus() == TokenStatus.ACTIVE));
    }
}
