package com.cleber.financas.service.impl;

import com.cleber.financas.exception.ErroValidacaoException;
import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.model.repository.UsuarioRepository;
import com.cleber.financas.security.ratelimit.LimiteTentativasService;
import com.cleber.financas.service.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class UsuarioServiceImplTest {

    private UsuarioServiceImpl service;
    private UsuarioRepository repository;
    private PasswordEncoder passwordEncoder;
    private Validator validator;
    private LimiteTentativasService limiteTentativasService;
    private com.cleber.financas.service.token.RefreshTokenService refreshTokenService;

    @Before
    public void setUp() {
        repository = Mockito.mock(UsuarioRepository.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        limiteTentativasService = Mockito.mock(LimiteTentativasService.class);
        refreshTokenService = Mockito.mock(com.cleber.financas.service.token.RefreshTokenService.class);
        Mockito.when(limiteTentativasService.isBloqueado(Mockito.anyString())).thenReturn(false);
        service = new UsuarioServiceImpl(repository, passwordEncoder, validator,
                limiteTentativasService, refreshTokenService,
                Mockito.mock(AuthenticationManager.class), Mockito.mock(JwtService.class),
                5, 2, 3);
    }

    @Test
    public void deveLancarErroValidacaoQuandoUsuarioNaoTiverNome() {
        Usuario usuario = Usuario.builder()
                .nomeCompleto("") // Vazio
                .cpf("12345678901")
                .nomeUsuario("user")
                .email("test@test.com")
                .senha("123")
                .build();

        Throwable exception = Assertions.catchThrowable(() -> service.validarUsuario(usuario));

        Assertions.assertThat(exception)
                .isInstanceOf(ErroValidacaoException.class);
    }

    @Test
    public void deveLancarErroValidacaoQuandoEmailForInvalido() {
        Usuario usuario = Usuario.builder()
                .nomeCompleto("Nome Teste")
                .cpf("12345678901")
                .nomeUsuario("user")
                .email("email-invalido")
                .senha("123")
                .build();

        Throwable exception = Assertions.catchThrowable(() -> service.validarUsuario(usuario));

        Assertions.assertThat(exception)
                .isInstanceOf(ErroValidacaoException.class);
    }

    @Test
    public void naoDeveLancarErroQuandoUsuarioForValido() {
        Usuario usuario = Usuario.builder()
                .nomeCompleto("Nome Teste")
                .cpf("12345678901")
                .nomeUsuario("user")
                .email("test@test.com")
                .senha("123")
                .build();

        Assertions.assertThatCode(() -> service.validarUsuario(usuario))
                .doesNotThrowAnyException();
    }

    @Test
    public void deveLancarErroQuandoUsuarioNaoEncontradoAoTrocarSenha() {
        java.util.UUID usuarioId = java.util.UUID.randomUUID();
        Mockito.when(repository.findById(usuarioId)).thenReturn(java.util.Optional.empty());

        Throwable exception = Assertions.catchThrowable(() ->
                service.trocarSenha(usuarioId, "senhaAtual", "novaSenha"));

        Assertions.assertThat(exception)
                .isInstanceOf(ErroValidacaoException.class)
                .hasMessage("Usuário não encontrado");
    }

    @Test
    public void deveLancarErroQuandoSenhaAtualNaoBaterAoTrocarSenha() {
        java.util.UUID usuarioId = java.util.UUID.randomUUID();
        Usuario usuario = Usuario.builder().uuid(usuarioId).senha("hashAntigo").build();
        Mockito.when(repository.findById(usuarioId)).thenReturn(java.util.Optional.of(usuario));
        Mockito.when(passwordEncoder.matches("senhaIncorreta", "hashAntigo")).thenReturn(false);

        Throwable exception = Assertions.catchThrowable(() ->
                service.trocarSenha(usuarioId, "senhaIncorreta", "novaSenha"));

        Assertions.assertThat(exception)
                .isInstanceOf(ErroValidacaoException.class)
                .hasMessage("A senha atual informada está incorreta");
        Mockito.verify(refreshTokenService, Mockito.never()).revogarAllByUser(Mockito.any());
    }

    @Test
    public void deveTrocarSenhaComSucessoERevogarSessoes() {
        java.util.UUID usuarioId = java.util.UUID.randomUUID();
        Usuario usuario = Usuario.builder().uuid(usuarioId).senha("hashAntigo").build();
        Mockito.when(repository.findById(usuarioId)).thenReturn(java.util.Optional.of(usuario));
        Mockito.when(passwordEncoder.matches("senhaCorreta", "hashAntigo")).thenReturn(true);
        Mockito.when(passwordEncoder.encode("novaSenha")).thenReturn("hashNovo");

        service.trocarSenha(usuarioId, "senhaCorreta", "novaSenha");

        Assertions.assertThat(usuario.getSenha()).isEqualTo("hashNovo");
        Mockito.verify(repository).save(usuario);
        Mockito.verify(refreshTokenService).revogarAllByUser(usuarioId);
    }

    @Test
    public void deveAlterarSenhaDiretamenteERevogarSessoes() {
        java.util.UUID usuarioId = java.util.UUID.randomUUID();
        Usuario usuario = Usuario.builder().uuid(usuarioId).senha("hashAntigo").build();
        Mockito.when(repository.findById(usuarioId)).thenReturn(java.util.Optional.of(usuario));
        Mockito.when(passwordEncoder.encode("novaSenha")).thenReturn("hashNovo");

        service.alterarSenha(usuarioId, "novaSenha");

        Assertions.assertThat(usuario.getSenha()).isEqualTo("hashNovo");
        Mockito.verify(repository).save(usuario);
        Mockito.verify(refreshTokenService).revogarAllByUser(usuarioId);
    }
}
