package com.cleber.financas.api.resource;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;

import com.cleber.financas.api.converter.UsuarioConverter;
import com.cleber.financas.api.cookie.RefreshTokenCookieFactory;
import com.cleber.financas.api.dto.AutenticacaoResultado;
import com.cleber.financas.api.dto.RequestLoginDTO;
import com.cleber.financas.api.dto.UsuarioDTO;
import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.model.repository.UsuarioRepository;
import com.cleber.financas.service.JwtService;
import com.cleber.financas.service.LancamentoService;
import com.cleber.financas.service.UsuarioService;
import com.cleber.financas.service.token.RefreshTokenEmitido;
import com.cleber.financas.service.token.RefreshTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = UsuarioController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class UsuarioResourceRestTest {

    private static final String API = "/api/auth";
    private static final MediaType JSON = MediaType.APPLICATION_JSON;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private LancamentoService lancamentoService;

    @MockBean
    private UsuarioConverter usuarioConverter;

    @MockBean
    private UsuarioRepository usuarioRepository;

    @MockBean
    private RefreshTokenService refreshTokenService;

    @MockBean
    private RefreshTokenCookieFactory cookieFactory;

    @MockBean
    private com.cleber.financas.security.SecurityUserDetailsService securityUserDetailsService;

    @MockBean
    private com.cleber.financas.security.ratelimit.LimiteTentativasService limiteTentativasService;

    @MockBean
    private com.cleber.financas.security.ratelimit.LoginLimiteTentativasFilter loginLimiteTentativasFilter;

    @org.junit.Before
    public void setUp() {
        this.mvc = MockMvcBuilders.standaloneSetup(new UsuarioController(
                jwtService,
                usuarioService,
                lancamentoService,
                usuarioConverter,
                refreshTokenService,
                cookieFactory
        ))
        .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
        .setControllerAdvice(new com.cleber.financas.api.common.GlobalExceptionHandler())
        .build();
    }

    @Test
    public void deveAutenticarUmUsuario() throws Exception {
        String email = "cleber@gmail.com";
        String senha = "senha123";
        RequestLoginDTO loginRequest = new RequestLoginDTO(email, senha);

        Duration ttl = Duration.ofDays(7);
        Mockito.when(usuarioService.autenticar(
                        Mockito.any(RequestLoginDTO.class), Mockito.anyString(), Mockito.any()))
                .thenReturn(new AutenticacaoResultado(
                        new com.cleber.financas.api.dto.TokenResponseDTO("jwt-accesstoken"),
                        new RefreshTokenEmitido("refresh-token", ttl)));
        Mockito.when(cookieFactory.buildSet("refresh-token", ttl))
                .thenReturn(org.springframework.http.ResponseCookie
                        .from("refresh_token", "refresh-token")
                        .httpOnly(true)
                        .path("/api/auth")
                        .maxAge(ttl)
                        .build());

        String json = new ObjectMapper().writeValueAsString(loginRequest);

        MockHttpServletRequestBuilder requisicao = MockMvcRequestBuilders
                .post(API + "/sign-in")
                .accept(JSON)
                .contentType(JSON)
                .content(json);

        mvc.perform(requisicao)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("accessToken").value("jwt-accesstoken"))
                .andExpect(MockMvcResultMatchers.jsonPath("tokenType").value("Bearer"))
                .andExpect(MockMvcResultMatchers.header().string("Set-Cookie",
                        org.hamcrest.Matchers.containsString("refresh_token=refresh-token")));
    }

    @Test
    public void deveRotacionarToken() throws Exception {
        String oldRefreshToken = "old-refresh-token";
        String newRefreshToken = "new-refresh-token";
        String newAccessToken = "new-access-token";
        Duration ttl = Duration.ofDays(7);

        Usuario usuario = Usuario.builder().uuid(UUID.randomUUID()).email("cleber@gmail.com").build();

        RefreshTokenEmitido emitido = new RefreshTokenEmitido(newRefreshToken, ttl);

        Mockito.when(refreshTokenService.rotacionar(Mockito.eq(oldRefreshToken), Mockito.anyString(), Mockito.any()))
                .thenReturn(emitido);
        Mockito.when(refreshTokenService.validar(newRefreshToken)).thenReturn(usuario);
        Mockito.when(jwtService.gerarToken(Mockito.eq(usuario), Mockito.anyList())).thenReturn(newAccessToken);
        Mockito.when(cookieFactory.buildSet(newRefreshToken, ttl))
                .thenReturn(org.springframework.http.ResponseCookie.from("refresh_token", newRefreshToken)
                        .httpOnly(true).path("/api/auth").maxAge(ttl).build());

        MockHttpServletRequestBuilder requisicao = MockMvcRequestBuilders
                .post(API + "/refresh")
                .accept(JSON)
                .cookie(new jakarta.servlet.http.Cookie("refresh_token", oldRefreshToken));

        mvc.perform(requisicao)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("accessToken").value(newAccessToken))
                .andExpect(MockMvcResultMatchers.header().string("Set-Cookie",
                        org.hamcrest.Matchers.containsString("refresh_token=" + newRefreshToken)));
    }

    @Test
    public void deveSalvarUmUsuario() throws Exception {
        String email = "cleber@gmail.com";
        String senha = "senha123";
        UUID id = UUID.randomUUID();

        UsuarioDTO dto = new UsuarioDTO.UsuarioBuilder()
                .setNomeCompleto("Cleber Silva")
                .setCpf("529.982.247-25")
                .setNomeUsuario("cleber")
                .setEmail(email)
                .setSenha(senha)
                .build();

        Usuario usuario = Usuario.builder()
                .uuid(id)
                .nomeCompleto(dto.getNomeCompleto())
                .cpf(dto.getCpf())
                .nomeUsuario(dto.getNomeUsuario())
                .email(email)
                .senha(senha)
                .build();

        Mockito.when(usuarioService.salvarUsuario(Mockito.any(Usuario.class))).thenReturn(usuario);
        String json = new ObjectMapper().writeValueAsString(dto);

        MockHttpServletRequestBuilder requisicao = MockMvcRequestBuilders
                .post(API + "/join/sign-up")
                .accept(JSON)
                .contentType(JSON)
                .content(json);

        mvc.perform(requisicao)
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("uuid").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("nomeCompleto").value(dto.getNomeCompleto()))
                .andExpect(MockMvcResultMatchers.jsonPath("nomeUsuario").value(dto.getNomeUsuario()))
                .andExpect(MockMvcResultMatchers.jsonPath("email").value(email));
    }

    @Test
    public void deveRetornarOSaldoDeUmUsuario() throws Exception {
        UUID id = UUID.randomUUID();
        BigDecimal saldo = BigDecimal.valueOf(1500);
        Usuario usuario = Usuario.builder().uuid(id).email("cleber@gmail.com").senha("senha123").build();

        Mockito.when(usuarioService.obterUsuarioPorId(id)).thenReturn(Optional.of(usuario));
        Mockito.when(lancamentoService.obterSaldoPorUsuario(id)).thenReturn(saldo);

        MockHttpServletRequestBuilder requisicao = MockMvcRequestBuilders
                .get(API + "/" + id + "/saldo")
                .accept(JSON)
                .contentType(JSON);

        mvc.perform(requisicao)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("1500"));
    }

    @Test
    public void deveResourceNaoEncontradoNaoExistirUsuarioParaObterSaldo() throws Exception {
        UUID id = UUID.randomUUID();

        Mockito.when(usuarioService.obterUsuarioPorId(id)).thenReturn(Optional.empty());

        MockHttpServletRequestBuilder requisicao = MockMvcRequestBuilders
                .get(API + "/" + id + "/saldo")
                .accept(JSON)
                .contentType(JSON);

        mvc.perform(requisicao)
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void deveTrocarSenhaComSucessoRetornando204() throws Exception {
        UUID usuarioUuid = UUID.randomUUID();
        com.cleber.financas.security.UsuarioAutenticado usuarioAutenticado =
                new com.cleber.financas.security.UsuarioAutenticado(
                        usuarioUuid,
                        "cleber@gmail.com",
                        "senhaAtual",
                        java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))
                );

        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuarioAutenticado, null, usuarioAutenticado.getAuthorities())
        );

        try {
            com.cleber.financas.api.dto.AlterarSenhaDTO dto =
                    new com.cleber.financas.api.dto.AlterarSenhaDTO("senhaAtual123", "novaSenha123");

            String json = new ObjectMapper().writeValueAsString(dto);

            MockHttpServletRequestBuilder requisicao = MockMvcRequestBuilders
                    .post(API + "/trocar-senha")
                    .accept(JSON)
                    .contentType(JSON)
                    .content(json);

            mvc.perform(requisicao)
                    .andExpect(MockMvcResultMatchers.status().isNoContent());

            Mockito.verify(usuarioService).trocarSenha(usuarioUuid, "senhaAtual123", "novaSenha123");
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }

    @Test
    public void deveRetornarBadRequestAoTrocarSenhaComSenhaAtualIncorreta() throws Exception {
        UUID usuarioUuid = UUID.randomUUID();
        com.cleber.financas.security.UsuarioAutenticado usuarioAutenticado =
                new com.cleber.financas.security.UsuarioAutenticado(
                        usuarioUuid,
                        "cleber@gmail.com",
                        "senhaAtual",
                        java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))
                );

        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuarioAutenticado, null, usuarioAutenticado.getAuthorities())
        );

        try {
            com.cleber.financas.api.dto.AlterarSenhaDTO dto =
                    new com.cleber.financas.api.dto.AlterarSenhaDTO("senhaIncorreta", "novaSenha123");

            Mockito.doThrow(new com.cleber.financas.exception.ErroValidacaoException("A senha atual informada está incorreta"))
                    .when(usuarioService).trocarSenha(usuarioUuid, "senhaIncorreta", "novaSenha123");

            String json = new ObjectMapper().writeValueAsString(dto);

            MockHttpServletRequestBuilder requisicao = MockMvcRequestBuilders
                    .post(API + "/trocar-senha")
                    .accept(JSON)
                    .contentType(JSON)
                    .content(json);

            mvc.perform(requisicao)
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }

    @Test
    public void deveRetornarBadRequestQuandoDtoDeTrocaDeSenhaInvalido() throws Exception {
        UUID usuarioUuid = UUID.randomUUID();
        com.cleber.financas.security.UsuarioAutenticado usuarioAutenticado =
                new com.cleber.financas.security.UsuarioAutenticado(
                        usuarioUuid,
                        "cleber@gmail.com",
                        "senhaAtual",
                        java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))
                );

        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuarioAutenticado, null, usuarioAutenticado.getAuthorities())
        );

        try {
            com.cleber.financas.api.dto.AlterarSenhaDTO dto =
                    new com.cleber.financas.api.dto.AlterarSenhaDTO("", "");

            String json = new ObjectMapper().writeValueAsString(dto);

            MockHttpServletRequestBuilder requisicao = MockMvcRequestBuilders
                    .post(API + "/trocar-senha")
                    .accept(JSON)
                    .contentType(JSON)
                    .content(json);

            mvc.perform(requisicao)
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }
}
