package com.cleber.financas.service;

import com.cleber.financas.exception.RegraDeNegocioException;
import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.model.repository.UsuarioRepository;
import com.cleber.financas.service.impl.UsuarioServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/*Não é necessario @DataJpaTest, lembrando que é possivel
 mockar bean gerenciado pelo spring framework, adiconar dentro
  do contexto do spring framework*/

@RunWith(SpringRunner.class) /*@MockBean, */
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
}
