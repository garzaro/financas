package com.cleber.financas.api.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import com.cleber.financas.api.dto.AlterarSenhaDTO;
import com.cleber.financas.api.dto.RequestLoginDTO;
import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.model.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class TrocaSenhaIntegrationTest {

    private static final String API = "/api/auth";
    private static final MediaType JSON = MediaType.APPLICATION_JSON;
    private static final String SENHA_ATUAL = "senhaAtual123";
    private static final String NOVA_SENHA = "novaSenha456";

    @Container
    static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.url",
                () -> "redis://" + REDIS.getHost() + ":" + REDIS.getMappedPort(6379));
    }

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Usuario usuario;
    private String email;

    @BeforeEach
    void setUp() {
        email = "troca-" + UUID.randomUUID() + "@gmail.com";
        usuario = usuarioRepository.save(Usuario.builder()
                .nomeCompleto("Teste Troca")
                .cpf(UUID.randomUUID().toString().replace("-", "").substring(0, 11))
                .nomeUsuario("troca")
                .email(email)
                .senha(passwordEncoder.encode(SENHA_ATUAL))
                .isAtivo(true)
                .build());
    }

    @Test
    void semTokenRetorna401() throws Exception {
        mvc.perform(post(API + "/trocar-senha")
                        .contentType(JSON)
                        .content(objectMapper.writeValueAsString(
                                new AlterarSenhaDTO(SENHA_ATUAL, NOVA_SENHA))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void trocaSenhaRevogaRefreshAntigoEAceitaSenhaNova() throws Exception {
        MvcResult login = signIn(SENHA_ATUAL);
        String accessToken = objectMapper.readTree(login.getResponse().getContentAsString())
                .get("accessToken").asText();
        Cookie refreshAntigo = login.getResponse().getCookie("refresh_token");
        assertThat(refreshAntigo).isNotNull();

        mvc.perform(post(API + "/trocar-senha")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(JSON)
                        .content(objectMapper.writeValueAsString(
                                new AlterarSenhaDTO(SENHA_ATUAL, NOVA_SENHA))))
                .andExpect(status().isNoContent());

        Usuario atualizado = usuarioRepository.findById(usuario.getUuid()).orElseThrow();
        assertThat(passwordEncoder.matches(NOVA_SENHA, atualizado.getSenha())).isTrue();

        mvc.perform(post(API + "/refresh").cookie(refreshAntigo))
                .andExpect(status().isUnauthorized());

        mvc.perform(post(API + "/sign-in")
                        .contentType(JSON)
                        .content(objectMapper.writeValueAsString(
                                new RequestLoginDTO(email, NOVA_SENHA))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void novaSenhaComMenosDeSeisCaracteresRetorna400() throws Exception {
        MvcResult login = signIn(SENHA_ATUAL);
        String accessToken = objectMapper.readTree(login.getResponse().getContentAsString())
                .get("accessToken").asText();

        mvc.perform(post(API + "/trocar-senha")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(JSON)
                        .content(objectMapper.writeValueAsString(
                                new AlterarSenhaDTO(SENHA_ATUAL, "12345"))))
                .andExpect(status().isBadRequest());
    }

    private MvcResult signIn(String senha) throws Exception {
        return mvc.perform(post(API + "/sign-in")
                        .contentType(JSON)
                        .content(objectMapper.writeValueAsString(
                                new RequestLoginDTO(email, senha))))
                .andExpect(status().isOk())
                .andReturn();
    }
}
