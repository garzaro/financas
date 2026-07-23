package com.cleber.financas.api.resource;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.cleber.financas.api.converter.UsuarioConverter;
import com.cleber.financas.service.JwtService;
import com.cleber.financas.service.LancamentoService;
import com.cleber.financas.service.UsuarioService;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = UsuarioController.class)
@ActiveProfiles("test")
public class UsuarioValidationMessagesTest {

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
    public void deveRetornarMensagemDoMessagesPropertiesQuandoNomeCompletoForInvalido() throws Exception {
        String payload = """
                {
                  \"nomeCompleto\": \"\",
                  \"cpf\": \"52998224725\",
                  \"nomeUsuario\": \"cleber\",
                  \"email\": \"cleber@gmail.com\",
                  \"senha\": \"senha123\"
                }
                """;

        mvc.perform(post("/api/auth/join/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.erros[*].campo", hasItem("nomeCompleto")))
          .andExpect(jsonPath("$.erros[*].mensagem", hasItem("O nome completo é obrigatório!")));

        verifyNoInteractions(usuarioService);
    }

      @Test
      public void deveRetornarMensagemDoMessagesPropertiesQuandoLoginVierSemEmail() throws Exception {
        String payload = """
            {
              \"email\": \"\",
              \"senha\": \"senha123\"
            }
            """;

        mvc.perform(post("/api/auth/sign-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.erros[0].campo").value("email"))
            .andExpect(jsonPath("$.erros[0].mensagem").value("O email é obrigatório!"));

        verifyNoInteractions(authenticationManager);
      }
}
