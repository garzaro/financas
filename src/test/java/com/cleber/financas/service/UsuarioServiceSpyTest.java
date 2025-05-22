package com.cleber.financas.service;

import com.cleber.financas.exception.RegraDeNegocioException;
import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.model.repository.UsuarioRepository;
import com.cleber.financas.service.impl.UsuarioServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class UsuarioServiceSpyTest {
    @SpyBean
    UsuarioServiceImpl usuarioServiceImpl;
    @MockBean
    UsuarioRepository usuarioRepository;

    @Test(expected = Test.None.class)
    public void deveSalvarUmUsuario(){
        /*cenario*/
        Mockito.doNothing().when(usuarioServiceImpl)
                .validarDuplicidadeEmailCpf("email@gmail.com",Mockito.anyString());

        Usuario usuario = criarUsuario();

        Mockito.when(usuarioRepository.save(Mockito.any(Usuario.class)))
                .thenReturn(usuario);

        /*ação*/
        Usuario usuarioSalvo = usuarioServiceImpl
                .salvarUsuario(new Usuario());

        /*verificação*/
        Assertions.assertThat(usuarioSalvo)
                .isNotNull();
        Assertions.assertThat(usuarioSalvo.getId())
                .isEqualTo(1l);
        Assertions.assertThat(usuarioSalvo.getNome())
                .isEqualTo("garzaro74");
        Assertions.assertThat(usuarioSalvo.getEmail())
                .isEqualTo("email@gmail.com");
        Assertions.assertThat(usuarioSalvo.getSenha())
                .isEqualTo("senha");

    }
    @Test(expected = RegraDeNegocioException.class)
    public void naoDeveSalvarUmUsuarioComEmailJaCadastrado(){

        Usuario persistirUsuario = criarUsuario();

        Mockito.doThrow(RegraDeNegocioException.class)
                .when(usuarioServiceImpl)
                .validarDuplicidadeEmailCpf("email@gmail.com","123456789-00");
        /*ação*/
        usuarioServiceImpl.salvarUsuario(persistirUsuario);

        /*verificação*/
        Mockito.verify(usuarioRepository, Mockito.never())
                .save(persistirUsuario);
    }
    /*criar instacias*/
    public static Usuario criarUsuario() {
        return Usuario.builder()
                .id(1l)
                .usuario("garzaro74")
                .email("email@gmail.com")
                .senha("senha")
                .build();

    }
}
