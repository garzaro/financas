package com.cleber.financas.service.impl;

import com.cleber.financas.exception.ErroValidacaoException;
import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.model.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class UsuarioServiceImplTest {

    private UsuarioServiceImpl service;
    private UsuarioRepository repository;
    private PasswordEncoder passwordEncoder;
    private Validator validator;

    @Before
    public void setUp() {
        repository = Mockito.mock(UsuarioRepository.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        service = new UsuarioServiceImpl(repository, passwordEncoder, validator);
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
}
