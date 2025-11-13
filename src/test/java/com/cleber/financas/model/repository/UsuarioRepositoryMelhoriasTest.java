package com.cleber.financas.model.repository;

import com.cleber.financas.model.entity.Usuario;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")

/*@DataJpaTest - Sobrescreve qualquer configuração
 no banco de teste, cria uma instancia no bd h2, ao
  finalizar ela deleta da memoria, rollback após
  a execução de cada metodo*/
@DataJpaTest

/*@AutoConfigureTestDatabase - mantem as configurações do banco de teste h2
 (DataJpaTest, desconfiguratudo)*/
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UsuarioRepositoryMelhoriasTest {
    
    /*Inicio teste com Dougllas Sousa (Udemy)*/

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    /*TestEntityManager - Responsavel pelas operações na base de dados, CRUD
    * este é configurado apenas para testes, nao é o real*/
    TestEntityManager entityManager;
    
    @Test
    public void deveVerificarAExistenciaDeUmEmailNaBaseDeDados() {
        /*cenario*/
        Usuario usuarioDeTeste = criarUsuario();
        entityManager.persist(usuarioDeTeste);

        /*execução/ação*/
        boolean verficarSeExisteEmail = usuarioRepository
                .existsByEmail("cricri@gmail.com");
        /*verficação*/
        Assertions.assertThat(verficarSeExisteEmail).isTrue();
        
    }
    @Test
    public void deveRetornarFalsoQuandoNaoHouverUsuarioCadastradoComOEmail() {
        /*cenario, - com o @DataJpaTest nao é necessario delete por causa do rollback*/
        /*usuarioRepository.deleteAll();*/

        boolean verificarSeExisteUsuarioCadastradoComEmail = usuarioRepository
                .existsByEmail("cricri@gmail.com");
        
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
        Assertions.assertThat(usuarioPersistido.getId()).isNotNull();
    }
    @Test
    public void deveBuscarUmUsuarioPeloEmail(){
        /*cenario*/
        Usuario salvandoUsuario = criarUsuario();
        usuarioRepository.save(salvandoUsuario);
        /*ação*/
        Optional<Usuario> pesquisarUsuarioPeloEmail = usuarioRepository
                .findByEmail("cricri@gmail.com");
        /*verificação*/
        Assertions.assertThat(pesquisarUsuarioPeloEmail.isPresent()).isTrue();
    }
    @Test
    public void deveRetornarVazioAoBuscarUsuarioPorEmailQueNaoExisteNaBase() {
        /*cenario, - com o @DataJpaTest nao é necessario delete por causa do rollback*/
        /*usuarioRepository.deleteAll();*/
        /*ação*/
        Optional<Usuario> usuarioInexistente =usuarioRepository
                .findByEmail("cricri@gmail.com");
        /*verificação*/
        Assertions.assertThat(usuarioInexistente.isPresent()).isFalse();
    }

    private static Usuario criarUsuario(){
        return Usuario.builder()
                .nome("Madonna da Silva")
                .nomeUsuario("cricri")
                .cpf("123.456.789.00")
                .email("cricri@gmail.com")
                .senha("123456")
                .dataCadastro(Instant.now())
                .build();
    }
}
