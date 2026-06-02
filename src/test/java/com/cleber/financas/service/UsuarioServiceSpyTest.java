package com.cleber.financas.service;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.cleber.financas.exception.RegraDeNegocioException;
import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.model.repository.UsuarioRepository;
import com.cleber.financas.service.impl.UsuarioServiceImpl;

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
                .validarEmailCpf(Mockito.anyString(), Mockito.anyString(), Mockito.any());

        Usuario usuario = criarUsuario();

        Mockito.when(usuarioRepository.save(Mockito.any(Usuario.class)))
                .thenReturn(usuario);

        /*ação*/
        Usuario persistir = criarUsuario();
        persistir.setId(null);
        Usuario usuarioSalvo = usuarioServiceImpl
                .salvarUsuario(persistir);

        /*verificação*/
        Assertions.assertThat(usuarioSalvo)
                .isNotNull();
        Assertions.assertThat(usuarioSalvo.getId())
                .isEqualTo(1l);
        Assertions.assertThat(usuarioSalvo.getNomeCompleto())
                .isEqualTo("Cleber Garzaro");
        Assertions.assertThat(usuarioSalvo.getEmail())
                .isEqualTo("email@gmail.com");
        Assertions.assertThat(usuarioSalvo.getSenha())
                .isNotNull();

    }
    @Test(expected = RegraDeNegocioException.class)
    public void naoDeveSalvarUmUsuarioComEmailJaCadastrado(){

        Usuario persistirUsuario = criarUsuario();

        Mockito.doThrow(RegraDeNegocioException.class)
                .when(usuarioServiceImpl)
                .validarEmailCpf(Mockito.anyString(), Mockito.anyString(), Mockito.any());
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
                .nomeCompleto("Cleber Garzaro")
                .cpf("12345678900")
                .nomeUsuario("garzaro74")
                .email("email@gmail.com")
                .senha("senha123")
                .build();

    }
}
