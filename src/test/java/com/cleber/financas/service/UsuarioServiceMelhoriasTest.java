package com.cleber.financas.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import com.cleber.financas.api.dto.AutenticacaoResultado;
import com.cleber.financas.api.dto.RequestLoginDTO;
import com.cleber.financas.exception.RegraDeNegocioException;
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
import org.springframework.transaction.annotation.Transactional;

import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.model.repository.UsuarioRepository;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@Transactional
public class UsuarioServiceMelhoriasTest {

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
        /*ação: confirma que e-mail e CPF ainda não estão cadastrados*/
        usuarioService.validarEmailCpf("cleber@gmail.com", "12345678900");
    }
    @Test(expected = RegraDeNegocioException.class)
    public void deveLancarErroAoValidarQuandoExistirEmaiLCadastrado(){
        /*cenario*/
        Usuario cadastrarEmail = criarUsuario();
        /*salvar*/
        usuarioRepository.save(cadastrarEmail);
        /*ação*/
        usuarioService.validarEmailCpf(cadastrarEmail.getEmail(), cadastrarEmail.getCpf());
    }
    @Test(expected = Test.None.class)
    public void deveAutenticarUmUsuarioComSucesso(){
        /*cenario*/
        Usuario persistirUsuario = criarUsuario();

        /*ação*/
        Usuario usuarioSalvo = usuarioService.salvarUsuario(persistirUsuario);

        Mockito.when(refreshTokenService.gerar(
                        Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(new RefreshTokenEmitido("refresh-token", Duration.ofDays(7)));

        /*verificar se o metodo autenticar funciona corretamente*/
        AutenticacaoResultado resultado = usuarioService.autenticar(
            new RequestLoginDTO(usuarioSalvo.getEmail(), "senha"), "127.0.0.1", "JUnit");

        /*verificação*/
        Assertions.assertThat(resultado).isNotNull();
        Assertions.assertThat(resultado.tokens().accessToken()).isNotBlank();
    }

    /*para criar instancias*/
    public static Usuario criarUsuario(){
        return Usuario.builder()
                .nomeCompleto("Cleber")
                .nomeUsuario("garzaro")
            .cpf("12345678900")
                .email("cleber@gmail.com")
                .senha("senha")
                .dataCadastro(LocalDateTime.now())
                .build();

    }


}
