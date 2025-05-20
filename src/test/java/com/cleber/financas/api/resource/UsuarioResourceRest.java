package com.cleber.financas.api.resource;

import com.cleber.financas.api.dto.UsuarioAutenticacaoDTO;
import com.cleber.financas.exception.ErroDeAutenticacao;
import com.cleber.financas.exception.RegraDeNegocioException;
import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.service.LancamentoService;
import com.cleber.financas.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.util.Optional;

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
    @MockBean
    LancamentoService lancamentoService;
    
    @Test
    public void deveAutenticarUmUsuario() throws Exception {
        /*cenario*/
        String email = "cleber@gmail.com";
        String senha = "senha";
        UsuarioAutenticacaoDTO dto = UsuarioAutenticacaoDTO.builder() /*json a ser enviado*/
                .email(email)
                .senha(senha)
                .build();
        
        Usuario usuario = Usuario.builder() /*usuario autenticado*/
                .id(1l)
                .email(email)
                .senha(senha)
                .build();
        /*simulação de autenticação*/
        Mockito.when(usuarioService.autenticar("cleber@gmail.com", "senha")).thenReturn(usuario);
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
                .andExpect(MockMvcResultMatchers.jsonPath("nomeCompleto").value(usuario.getNome()))
                .andExpect(MockMvcResultMatchers.jsonPath("nomeUsuario").value(usuario.getUsuario()))
                .andExpect(MockMvcResultMatchers.jsonPath("email").value(usuario.getEmail()))
                //.andExpect(MockMvcResultMatchers.jsonPath("senha").value(usuario.getSenha()))
                .andExpect(MockMvcResultMatchers.jsonPath("cadastroPessoaFisica")
                        .value(usuario.getCpf()));
    }
    
    @Test
    public void deveRetornarBadRequestQuandoHouverErroDeAutenticao() throws Exception {
        /*cenario*/
        String email = "cleber@gmail.com";
        String senha = "senha";
        UsuarioAutenticacaoDTO dto = UsuarioAutenticacaoDTO.builder() /*json a ser enviado*/
                .email(email)
                .senha(senha)
                .build();
        /*simulação de autenticação*/
        Mockito.when(usuarioService.autenticar(email, senha)).thenThrow(ErroDeAutenticacao.class);
        /*para transformar objeto de qualquer tipo em string json*/
        String json = new ObjectMapper().writeValueAsString(dto);
        
        /*ação e verificação*/
        MockHttpServletRequestBuilder requisicao = MockMvcRequestBuilders
                .post(API.concat("/autenticar"))
                .accept(JSON) /*aceita receber objeto do tipo json*/
                .contentType(JSON) /*enviar conteudo json*/
                .content(json); /*passar o objeto json*/
        
        mvc.perform(requisicao)
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
    
    @Test
    public void deveSalvarUmUsuario() throws Exception {
        /*cenario*/
        String email = "cleber@gmail.com";
        String senha = "senha";
        UsuarioAutenticacaoDTO dto = UsuarioAutenticacaoDTO.builder() /*json a ser enviado*/
                .email(email)
                .senha(senha)
                .build();
        
        Usuario usuario = Usuario.builder() /*usuario autenticado*/
                .id(1l)
                .email(email)
                .senha(senha)
                .build();
        /*simulação de autenticação*/
        Mockito.when(usuarioService.salvarUsuario(Mockito.any(Usuario.class))).thenReturn(usuario);
        /*para transformar objeto de qualquer tipo em string json*/
        String json = new ObjectMapper().writeValueAsString(dto);
        
        /*ação e verificação*/
        MockHttpServletRequestBuilder requisicao = MockMvcRequestBuilders
                .post(API)
                .accept(JSON) /*aceita receber objeto do tipo json*/
                .contentType(JSON) /*enviar conteudo json*/
                .content(json); /*passar o objeto json*/
        
        mvc.perform(requisicao)
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value(usuario.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("nomeCompleto").value(usuario.getNome()))
                .andExpect(MockMvcResultMatchers.jsonPath("nomeUsuario").value(usuario.getUsuario()))
                .andExpect(MockMvcResultMatchers.jsonPath("email").value(usuario.getEmail()))
                //.andExpect(MockMvcResultMatchers.jsonPath("senha").value(usuario.getSenha()))
                .andExpect(MockMvcResultMatchers.jsonPath("cadastroPessoaFisica")
                        .value(usuario.getCpf()));
    }
    
    @Test
    public void deveRetornarBadRequestAoTentarSalvarUmUsuarioInvalido() throws Exception {
        /*cenario*/
        String email = "cleber@gmail.com";
        String senha = "senha";
        UsuarioAutenticacaoDTO dto = UsuarioAutenticacaoDTO.builder() /*json a ser enviado*/
                .email(email)
                .senha(senha)
                .build();
        /*simulação de autenticação*/
        Mockito.when(usuarioService.salvarUsuario(Mockito.any(Usuario.class))).thenThrow(RegraDeNegocioException.class);
        /*para transformar objeto de qualquer tipo em string json*/
        String json = new ObjectMapper().writeValueAsString(dto);
        
        /*ação e verificação*/
        MockHttpServletRequestBuilder requisicao = MockMvcRequestBuilders
                .post(API)
                .accept(JSON) /*aceita receber objeto do tipo json*/
                .contentType(JSON) /*enviar conteudo json*/
                .content(json); /*passar o objeto json*/
        
        mvc.perform(requisicao).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
    
    @Test
    public void deveRetornarOSaldoDeUmUsuario() throws Exception {
        /*cenario*/
        String email = "cleber@gmail.com";
        String senha = "senha";
        Long id = 1l;
        
        BigDecimal saldo = BigDecimal.valueOf(1500);
        Usuario usuario = Usuario.builder().id(id).email(email).senha(senha).build();
        /*simulação de autenticação*/
        Mockito.when(usuarioService.obterUsuarioPorId(id)).thenReturn(Optional.of(usuario));
        Mockito.when(lancamentoService.obterSaldoPorUsuario(id)).thenReturn(saldo);
        /*ação e verificação*/
        MockHttpServletRequestBuilder requisicao = MockMvcRequestBuilders
                .get(API.concat("/1/saldo"))
                .accept(JSON) /*aceita receber objeto do tipo json*/
                .contentType(JSON); /*enviar conteudo json*/
        
        mvc.perform(requisicao)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("1500"));
    }
    
    @Test
    public void deveResourceNaoEncontradoNaoExistirUsuarioParaObterSaldo() throws Exception {
        /*cenario*/
        Long id = 1l;
        /*simulação de autenticação*/
        Mockito.when(usuarioService.obterUsuarioPorId(id)).thenReturn(Optional.empty());
        /*ação e verificação*/
        MockHttpServletRequestBuilder requisicao = MockMvcRequestBuilders
                .get(API.concat("/1/saldo"))
                .accept(JSON) /*aceita receber objeto do tipo json*/
                .contentType(JSON); /*enviar conteudo json*/
        
        mvc.perform(requisicao)
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
}
