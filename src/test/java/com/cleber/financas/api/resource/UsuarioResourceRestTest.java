package com.cleber.financas.api.resource;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.cleber.financas.api.converter.UsuarioConverter;
import com.cleber.financas.api.dto.LoginRequest;
import com.cleber.financas.api.dto.UsuarioDTO;
import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.service.JwtService;
import com.cleber.financas.service.LancamentoService;
import com.cleber.financas.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = UsuarioController.class)
@ActiveProfiles("test")
public class UsuarioResourceRestTest {

    private static final String API = "/api/auth";
    private static final MediaType JSON = MediaType.APPLICATION_JSON;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private LancamentoService lancamentoService;

    @MockBean
    private UsuarioConverter usuarioConverter;

    @Test
    public void deveAutenticarUmUsuario() throws Exception {
        String email = "cleber@gmail.com";
        String senha = "senha123";
        LoginRequest request = new LoginRequest(email, senha);

        org.springframework.security.core.userdetails.UserDetails userDetails =
                new org.springframework.security.core.userdetails.User(
                        email,
                        senha,
                        java.util.Collections.emptyList());

        Mockito.when(authenticationManager.authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(userDetails, null));
        Mockito.when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        Mockito.when(jwtService.gerarToken(userDetails)).thenReturn("jwt-token");

        String json = new ObjectMapper().writeValueAsString(request);

        MockHttpServletRequestBuilder requisicao = MockMvcRequestBuilders
                .post(API + "/login")
                .accept(JSON)
                .contentType(JSON)
                .content(json);

        mvc.perform(requisicao)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("token").value("jwt-token"))
                .andExpect(MockMvcResultMatchers.jsonPath("tokenType").value("Bearer"));
    }

    @Test
    public void deveSalvarUmUsuario() throws Exception {
        String email = "cleber@gmail.com";
        String senha = "senha123";
        UUID id = UUID.randomUUID();

        UsuarioDTO dto = new UsuarioDTO.UsuarioBuilder()
                .setNomeCompleto("Cleber Silva")
                .setCpf("123.456.789-00")
                .setNomeUsuario("cleber")
                .setEmail(email)
                .setSenha(senha)
                .build();

        Usuario usuario = Usuario.builder()
                .id(id)
                .nomeCompleto(dto.getNomeCompleto())
                .cpf(dto.getCpf())
                .nomeUsuario(dto.getNomeUsuario())
                .email(email)
                .senha(senha)
                .build();

        Mockito.when(usuarioService.salvarUsuario(Mockito.any(Usuario.class))).thenReturn(usuario);
        String json = new ObjectMapper().writeValueAsString(dto);

        MockHttpServletRequestBuilder requisicao = MockMvcRequestBuilders
                .post(API + "/join/sign-up/")
                .accept(JSON)
                .contentType(JSON)
                .content(json);

        mvc.perform(requisicao)
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value(id.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("nomeCompleto").value(dto.getNomeCompleto()))
                .andExpect(MockMvcResultMatchers.jsonPath("nomeUsuario").value(dto.getNomeUsuario()))
                .andExpect(MockMvcResultMatchers.jsonPath("email").value(email));
    }

    @Test
    public void deveRetornarOSaldoDeUmUsuario() throws Exception {
        UUID id = UUID.randomUUID();
        BigDecimal saldo = BigDecimal.valueOf(1500);
        Usuario usuario = Usuario.builder().id(id).email("cleber@gmail.com").senha("senha123").build();

        Mockito.when(usuarioService.obterUsuarioPorId(id)).thenReturn(Optional.of(usuario));
        Mockito.when(lancamentoService.obterSaldoPorUsuario(id)).thenReturn(saldo);

        MockHttpServletRequestBuilder requisicao = MockMvcRequestBuilders
                .get(API + "/" + id + "/saldo")
                .accept(JSON)
                .contentType(JSON);

        mvc.perform(requisicao)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("1500"));
    }

    @Test
    public void deveResourceNaoEncontradoNaoExistirUsuarioParaObterSaldo() throws Exception {
        UUID id = UUID.randomUUID();

        Mockito.when(usuarioService.obterUsuarioPorId(id)).thenReturn(Optional.empty());

        MockHttpServletRequestBuilder requisicao = MockMvcRequestBuilders
                .get(API + "/" + id + "/saldo")
                .accept(JSON)
                .contentType(JSON);

        mvc.perform(requisicao)
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
}
