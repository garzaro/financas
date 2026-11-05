package com.cleber.financas.service;

import com.cleber.financas.api.dto.AutenticacaoResultado;
import com.cleber.financas.api.dto.RequestLoginDTO;
import com.cleber.financas.exception.ErroDeAutenticacao;
import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.model.repository.UsuarioRepository;
import com.cleber.financas.security.ratelimit.LimiteTentativasService;
import com.cleber.financas.service.impl.UsuarioServiceImpl;
import com.cleber.financas.service.token.RefreshTokenEmitido;
import com.cleber.financas.service.token.RefreshTokenService;
import jakarta.validation.Validation;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class UsuarioServiceMockTest {

    @MockBean
    UsuarioRepository usuarioRepository;

    UsuarioService usuarioService;
    AuthenticationManager authenticationManager;
    JwtService jwtService;
    LimiteTentativasService limiteTentativasService;
    RefreshTokenService refreshTokenService;

    @Before
    public void setUp() {
        usuarioRepository = Mockito.mock(UsuarioRepository.class);
        PasswordEncoder passwordEncoder = Mockito.mock(PasswordEncoder.class);
        authenticationManager = Mockito.mock(AuthenticationManager.class);
        jwtService = Mockito.mock(JwtService.class);
        limiteTentativasService = Mockito.mock(LimiteTentativasService.class);
        refreshTokenService = Mockito.mock(RefreshTokenService.class);
        Mockito.when(limiteTentativasService.isBloqueado(Mockito.anyString())).thenReturn(false);

        usuarioService = new UsuarioServiceImpl(
                usuarioRepository,
                passwordEncoder,
                Validation.buildDefaultValidatorFactory().getValidator(),
                limiteTentativasService,
                refreshTokenService,
                authenticationManager,
                jwtService,
                5, 900, 900
        );
    }

    @Test(expected = Test.None.class)
    public void deveAutenticarUmUsuarioComSucesso() {
        String email = "cleber@gmail.com";
        String senha = "senha";

        Usuario criarUsuario = Usuario.builder()
                .email(email)
                .senha(senha)
                .build();
        Mockito.when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(criarUsuario));
        Mockito.when(jwtService.gerarToken(criarUsuario, List.of("USER"))).thenReturn("token-jwt");
        Mockito.when(refreshTokenService.gerar(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(new RefreshTokenEmitido("refresh-token", Duration.ofDays(7)));

        AutenticacaoResultado resultadoAutenticacao = usuarioService.autenticar(
                new RequestLoginDTO(email, senha), "127.0.0.1", "JUnit");

        Assertions.assertThat(resultadoAutenticacao).isNotNull();
        Assertions.assertThat(resultadoAutenticacao.tokens().accessToken()).isEqualTo("token-jwt");
    }

    @Test(expected = ErroDeAutenticacao.class)
    public void DeveLancarErroQuandoNaoEncontrarUsuarioCadastradoComOEmailInformado() {
        Mockito.when(usuarioRepository.findByEmail(Mockito.anyString())).thenReturn(Optional.empty());

        String email = "cleber@gmail.com";
        String senha = "senha123";

        usuarioService.autenticar(new RequestLoginDTO(email, senha), "127.0.0.1", "JUnit");
    }

    @Test(expected = BadCredentialsException.class)
    public void deveLancarErroQuandoSenhaNaoBater() {
        String email = "cleber@gmail.com";
        String senha = "123";

        Mockito.when(authenticationManager.authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        usuarioService.autenticar(new RequestLoginDTO(email, senha), "127.0.0.1", "JUnit");
    }
}
