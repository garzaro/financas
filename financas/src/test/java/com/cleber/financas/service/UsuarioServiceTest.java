package com.cleber.financas.service;

import com.cleber.financas.exception.RegraDeNegocioException;
import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.model.repository.UsuarioRepository;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class UsuarioServiceTest {

    @Autowired
    UsuarioService usuarioService;

    @Autowired
    UsuarioRepository usuarioRepository;

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

        // Verificar se o metodo autenticarUsuario retorna o usuário autenticado corretamente
        Usuario usuarioAutenticado = usuarioService.autenticar(salvarUsuario.getEmail(), "senha123456");

        /*verificação*/
        Assertions.assertThat(usuarioAutenticado).isNotNull();
        Assertions.assertThat(usuarioAutenticado.getEmail()).isEqualTo(salvarUsuario.getEmail());
    }
}