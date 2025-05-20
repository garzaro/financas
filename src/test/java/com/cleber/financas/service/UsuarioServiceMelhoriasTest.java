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

import java.time.LocalDate;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class UsuarioServiceMelhoriasTest {
    
    @Autowired
    UsuarioService usuarioService;
    
    @Autowired
    UsuarioRepository usuarioRepository;
    
    @Test(expected = Test.None.class)
    public void deveValidarEmail(){
        /*cenario*/
        usuarioRepository.deleteAll();
        /*ação, sem verificação, só olha se existe o email*/
        usuarioService.
                validarEmailAndCpf("cleber@gmail.com","123456789-00");
    }
    @Test(expected = RegraDeNegocioException.class)
    public void deveLancarErroAoValidarQuandoExistirEmaiLCadastrado(){
        /*cenario*/
        Usuario cadastrarEmail = criarUsuario();
        /*salvar*/
        usuarioRepository.save(cadastrarEmail);
        /*ação*/
        usuarioService.
                validarEmailAndCpf("cleber@gmail.com", "123456789-00");
    }
    @Test(expected = Test.None.class)
    public void deveAutenticarUmUsuarioComSucesso(){
        /*cenario*/
        Usuario persistirUsuario = criarUsuario();

        /*ação*/
        Usuario usuarioSalvo = usuarioRepository.save(persistirUsuario);

        /*verificar se o metodo autenticarUsuario funciona corretamente*/
        Usuario usuarioAutenticado = usuarioService.autenticar(usuarioSalvo.getEmail(), usuarioSalvo.getSenha());

        /*verificação*/
        Assertions.assertThat(usuarioAutenticado).isNotNull();
        Assertions.assertThat(usuarioAutenticado.getEmail()).isEqualTo("cleber@gmail.com");
        Assertions.assertThat(usuarioAutenticado.getSenha()).isEqualTo("senha");
    }

    /*para criar instancias*/
    public static Usuario criarUsuario(){
        return Usuario.builder()
                .id(1l)
                .nome("Cleber")
                .usuario("garzaro")
                .cpf("123.456.789-00")
                .email("cleber@gmail.com")
                .senha("senha")
                .dataCadastro(LocalDate.now())
                .build();
    
    }
    
    
}