package com.cleber.financas.service;

import com.cleber.financas.exception.RegraDeNegocioException;
import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.model.repository.UsuarioRepository;
import com.cleber.financas.service.impl.UsuarioServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

/*Não é necessario @DataJpaTest, lembrando que é possivel
 mockar bean gerenciado pelo spring framework, adiconar dentro
  do contexto do spring framework*/

@RunWith(SpringRunner.class) /*@MockBean, mockar o contexto do spring framework*/
@ActiveProfiles("test")
public class UsuarioServiceMockTest {
    /*com o uso de mock nao é necessario injetar*/
    @MockBean
    UsuarioRepository usuarioRepository;

    UsuarioService usuarioService;

    /*metodo configurado*/
    @Before
    public void setUp(){
        /*instancia de usuario repository*/
        /*Usando o @MockBean*/
        usuarioRepository = Mockito.mock(UsuarioRepository.class);
        /*criando instancia real do usuario service*/
        usuarioService = new UsuarioServiceImpl(usuarioRepository);
    }

    @Test(expected = Test.None.class)
    public void deveValidarEmail(){
        /*cenario*/
        Mockito.when(usuarioRepository.existsByEmail(Mockito.anyString())).thenReturn(false);

        /*ação, sem verificação, só olha se existe o email*/
        usuarioService.validarEmailNaBaseDedados("cleber@gmail.com");
    }
    @Test(expected = RegraDeNegocioException.class)
    public void deveLancarErroAoValidarQuandoExistirEmaiLCadastrado(){
        /*cenario*/
        Mockito.when(usuarioRepository.existsByEmail(Mockito.anyString())).thenReturn(true);

        /*ação*/
        usuarioService.validarEmailNaBaseDedados("cleber@gmail.com");
    }

    @Test(expected = Test.None.class)
    public void deveAutenticarUmUsuarioComSucesso(){
        /*cenario*/
        String email = "cleber@gmail.com";
        String senha = "senha";

        /*Somente para o visualizar
        Usuario criarUmUsuario = Usuario.builder()
                .email("cleber@gmail.com")
                .senha("senha")
                .build();
       */
        Usuario criarUsuario = Usuario.builder()
                .email(email)
                .senha(senha)
                .build();
        Mockito.when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(criarUsuario));

        /*ação*/ /*deve retornar uma instancia de usuario autenticado*/
        Usuario resultadoAutenticacao = usuarioService.autenticarUsuario(email,senha);

        /*verificacao*/
        Assertions.assertThat(resultadoAutenticacao).isNotNull();

    }

    public void DeveLancarErroQuandoNaoEncontrarUsuarioCadastradoComOEmailInformado(){

    }

}
