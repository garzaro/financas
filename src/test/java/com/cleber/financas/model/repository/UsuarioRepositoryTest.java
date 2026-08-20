package com.cleber.financas.model.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.cleber.financas.model.entity.Usuario;

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
        Usuario usuarioDeTeste = criarUsuario();
        usuarioRepository.save(usuarioDeTeste);
        
        /*execução/ação*/
       boolean verficarSeExisteEmail = usuarioRepository.existsByEmail("usuario@gmail.com");
       
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
        Usuario persistindoUsuario = criarUsuario();
        /*ação*/
        Usuario usuarioPersistido = usuarioRepository.save(persistindoUsuario);
        
        /*verificação*/
        Assertions.assertThat(usuarioPersistido.getUuid()).isNotNull();
    }
    
    @Test
    public void deveBuscarUmUsuarioPeloEmail(){
        /*cenario*/
        Usuario salvandoUsuario = criarUsuario();

        usuarioRepository.save(salvandoUsuario);
        /*ação*/
        Optional<Usuario> pesquisarUsuarioPeloEmail = usuarioRepository
                .findByEmail("usuario@gmail.com");
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

    public static Usuario criarUsuario(){
        return Usuario.builder()
//                .id(UUID.randomUUID())
                .nomeCompleto("Cleber")
                .cpf("12345678978")
                .nomeUsuario("garzaro74")
                .email("usuario@gmail.com")
                .senha("senha")
                .dataCadastro(LocalDateTime.now())
                .build();

    }
}
