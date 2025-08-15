package com.cleber.financas.model.repository;

import com.cleber.financas.model.entity.Usuario;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class UsuarioRepositoryTest {
    /*Inicio teste com Dougllas Sousa (Udemy)*/
    @Autowired
    UsuarioRepository usuarioRepository;
   
    @Test
    public void deveVerificarAExistenciaDeUmEmailNaBaseDeDados() {
        /*cenario*/
        Usuario usuarioDeTeste = Usuario.builder()
                .nome("Madonna da Silva")
                .email("clebergarzaro74@gmail.com")
                .build();
        usuarioRepository.save(usuarioDeTeste);
        
        /*execução/ação*/
       boolean verficarSeExisteEmail = usuarioRepository.existsByEmail("clebergarzaro74@gmail.com");
       
       /*verficação*/
       Assertions.assertThat(verficarSeExisteEmail).isTrue();
       
    }
    
    @Test
    public void deveRetornarFalsoQuandoNaoHouverUsuarioCadastradoComOEmail() {
       /*cenario, não deve existir email na base*/
        usuarioRepository.deleteAll();
        boolean verificarSeExisteUsuarioCadastradoComEmail =usuarioRepository
                .existsByEmail("clebergarzaro@gmail.com");
        /*verificação*/
        Assertions.assertThat(verificarSeExisteUsuarioCadastradoComEmail).isFalse();
    }
    
    @Test
    public void devePersistirUsuarioNaBaseDeDados(){
       /*cenario*/
        Usuario persistindoUsuario = Usuario.builder()
                .nome("Cleber")
                .nomeUsuario("garzaro74")
                .email("clebergarzaro74@gmail.com")
                .senha("senha")
                .dataCadastro(LocalDateTime.now())
                .build();
        /*ação*/
        Usuario usuarioPersistido = usuarioRepository.save(persistindoUsuario);
        
        /*verificação*/
        Assertions.assertThat(usuarioPersistido.getId()).isNotNull();
    }
    
    @Test
    public void deveBuscarUmUsuarioPeloEmail(){
        /*cenario*/
        Usuario salvandoUsuario = Usuario.builder()
                .nomeUsuario("garzaro74")
                .email("clebergarzaro74@gmail.com")
                .build();
        usuarioRepository.save(salvandoUsuario);
        /*ação*/
        Optional<Usuario> pesquisarUsuarioPeloEmail = usuarioRepository
                .findByEmail("clebergarzaro74@gmail.com");
        /*verificação*/
        Assertions.assertThat(pesquisarUsuarioPeloEmail.isPresent()).isTrue();
    }
    
    @Test
    public void deveRetornarVazioAoBuscarUsuarioPorEmailQueNaoExisteNaBase() {
        /*cenario - nao presença de email*/
        usuarioRepository.deleteAll();
        /*ação*/
        Optional<Usuario> usuarioInexistente =usuarioRepository
                .findByEmail("clebergarzaro74@gmail.com");
        /*verificação*/
        Assertions.assertThat(usuarioInexistente.isPresent()).isFalse();
    }
}
