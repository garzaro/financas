package com.cleber.financas.service;

import com.cleber.financas.api.dto.AutenticacaoResultado;
import com.cleber.financas.exception.RegraDeNegocioException;
import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.model.repository.UsuarioRepository;
import com.cleber.financas.service.token.RefreshTokenEmitido;
import com.cleber.financas.service.token.RefreshTokenService;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Duration;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class UsuarioServiceTest {

    @Autowired
    UsuarioService usuarioService;

    @Autowired
    UsuarioRepository usuarioRepository;

    @MockBean
    RefreshTokenService refreshTokenService;

    @Test(expected = Test.None.class)
    public void deveValidarEmail(){
        /*cenario*/
        usuarioRepository.deleteAll();
        /*ação, sem verificação, só olha se existe o email**/
        usuarioService
                .validarEmailCpf("12345678900", "cleber@gmail.com");
    }
    @Test(expected = RegraDeNegocioException.class)
    public void deveLancarErroAoValidarQuandoExistirEmaiLCadastrado(){
        /*cenario*/
        Usuario cadastrarEmail = Usuario.builder()
                .nomeCompleto("Cleber Garzaro")
                .cpf("12345678900")
                .nomeUsuario("garzaro74")
                .email("cleber@gmail.com")
                .senha("Senha@123")
                .build();
        usuarioRepository.save(cadastrarEmail);
        /*ação*/
        usuarioService
                .validarEmailCpf("cleber@gmail.com", "12345678900");
    }
    @Test(expected = Test.None.class)
    public void deveAutenticarUmUsuarioComSucesso(){
        /*cenario*/
        Usuario usuario = Usuario.builder()
                .nomeCompleto("Cleber Garzaro")
                .cpf("12345678901")
                .nomeUsuario("garzaro75")
                .email("clebergarzaro@gmail.com")
                .senha("senha123456")
                .build();
        /*ação*/
        Usuario salvarUsuario = usuarioService.salvarUsuario(usuario);

        Mockito.when(refreshTokenService.gerar(
                        Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(new RefreshTokenEmitido("refresh-token", Duration.ofDays(7)));

        // Verificar se o metodo autenticar retorna os dois tokens corretamente
        AutenticacaoResultado resultado = usuarioService.autenticar(
                new com.cleber.financas.api.dto.RequestLoginDTO(salvarUsuario.getEmail(), "senha123456"),
                "127.0.0.1", "JUnit");

        /*verificação*/
        Assertions.assertThat(resultado).isNotNull();
        Assertions.assertThat(resultado.tokens().accessToken()).isNotBlank();
        Assertions.assertThat(resultado.refresh().rawToken()).isEqualTo("refresh-token");
    }
}
