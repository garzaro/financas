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
        /*ação, sem verificação, só olha se existe o email*/
        usuarioService
                .validarEmailAndCpf("cleber@gmail.com","123456789-00");
    }
    @Test(expected = RegraDeNegocioException.class)
    public void deveLancarErroAoValidarQuandoExistirEmaiLCadastrado(){
        /*cenario*/
        Usuario cadastrarEmail = Usuario.builder()
                .usuario("garzaro74")
                .email("cleber@gmail.com")
                .build();
        usuarioRepository.save(cadastrarEmail);
        /*ação*/
        usuarioService
                .validarEmailAndCpf("cleber@gmail.com", "123456789-00");
    }
    @Test(expected = Test.None.class)
    public void deveAutenticarUmUsuarioComSucesso(){
        /*cenario*/
        Usuario usuario = Usuario.builder()
                .email("clebergarzaro@gmail.com")
                .senha("senha123456")
                .build();
        /*ação*/
        Usuario salvarUsuario = usuarioRepository.save(usuario);

        // Verificar se o método autenticarUsuario retorna o usuário autenticado corretamente
        Usuario usuarioAutenticado = usuarioService.autenticar(salvarUsuario.getEmail(), salvarUsuario.getSenha());

        /*verificação*/
        Assertions.assertThat(usuarioAutenticado).isNotNull();
        Assertions.assertThat(usuarioAutenticado.getEmail()).isEqualTo(salvarUsuario.getEmail());
        Assertions.assertThat(usuarioAutenticado.getSenha()).isEqualTo(salvarUsuario.getSenha());
    }
    
}
