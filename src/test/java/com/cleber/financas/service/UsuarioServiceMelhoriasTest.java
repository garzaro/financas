package com.cleber.financas.service;

import com.cleber.financas.exception.RegraDeNegocioException;
import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.model.repository.UsuarioRepository;
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
        usuarioService.validarEmailNaBaseDedados("cleber@gmail.com");
    }
    @Test(expected = RegraDeNegocioException.class)
    public void deveLancarErroAoValidarQuandoExistirEmaiLCadastrado(){
        /*cenario*/
        Usuario cadastrarEmail = criarUsuario();
        /*salvar*/
        usuarioRepository.save(cadastrarEmail);
        /*ação*/
        usuarioService.validarEmailNaBaseDedados("cleber@gmail.com");
    }
    /*para criar instancias*/
    public static Usuario criarUsuario(){
        return Usuario.builder()
                .nomeCompleto("Cleber")
                .nomeUsuario("garzaro")
                .cadastroPessoaFisica("123.456.789-00")
                .email("cleber@gmail.com")
                .senha("123456")
                .dataCadastro(LocalDate.now())
                .build();
    
    }
    
    
}
