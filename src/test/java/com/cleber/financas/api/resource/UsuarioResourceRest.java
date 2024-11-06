package com.cleber.financas.api.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
/*@WebMvcTest(controllers = UsuarioController.class) - sobe o controller para teste
 * e define qual deles deve subir com o teste, serve para isolar das outras camadas*/
/*@AutoConfigureMockMvc - autoconfigura o MockMvc para mockar rest api*/
/*MockMvcRequestBuilders - objeto para criar requisição, verbos*/
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.cleber.financas.api.dto.UsuarioAutenticacaoDTO;
import com.cleber.financas.api.dto.UsuarioCadastroDTO;
import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.service.UsuarioService;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@WebMvcTest(controllers = UsuarioController.class)
@ActiveProfiles("test")
public class UsuarioResourceRest {

    static final String API = "/api/usuarios";
    static final MediaType JSON = MediaType.APPLICATION_JSON; /*constante*/

    @Autowired
    MockMvc mvc;
    @MockBean
    UsuarioService usuarioService;

    @Test
    public void deveAutenticarUmUsuario() throws Exception {
        /*cenario*/
        UsuarioAutenticacaoDTO dto = UsuarioAutenticacaoDTO.builder() /*json a ser enviado*/
                .email("cleber@gmail.com")
                .senha("123456").build();

        Usuario usuario = Usuario.builder() /*usuario autenticado*/
                .id(1l)
                .email("cleber@gmail.com")
                .senha("123456").build();
        /*simulação de autenticação*/
        Mockito.when(usuarioService.autenticarUsuario("cleber@gmail.com", "123456")).thenReturn(usuario);
        /*para transformar objeto de qualquer tipo em string json*/
        String json = new ObjectMapper().writeValueAsString(dto);

        /*ação e verificação*/
        MockHttpServletRequestBuilder requisicao = MockMvcRequestBuilders
                .post(API.concat("/autenticar"))
                .accept(JSON) /*aceita receber objeto do tipo json*/
                .contentType(JSON) /*enviar conteudo json*/
                .content(json); /*passar o objeto json*/

        mvc.perform(requisicao)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value(usuario.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("nome").value(usuario.getNomeCompleto()))
                .andExpect(MockMvcResultMatchers.jsonPath("nomeUsuario").value(usuario.getNomeUsuario()))
                .andExpect(MockMvcResultMatchers.jsonPath("emal").value(usuario.getEmail()))
                .andExpect(MockMvcResultMatchers.jsonPath("senha").value(usuario.getSenha()))
                .andExpect(MockMvcResultMatchers.jsonPath("cadastroPessoaFisica").value(usuario.getCadastroPessoaFisica()))
        ;
                /*considerar colocar jsonIgnore no dominio de usuario*/

    }


}
