package com.cleber.financa.service;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.cleber.financa.exceptions.ErroAutenticacao;
import com.cleber.financa.exceptions.RegraNegocioException;
import com.cleber.financa.model.entity.Usuario;
import com.cleber.financa.model.repository.UsuarioRepository;
import com.cleber.financa.service.impl.UsuarioServiceImpl;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class UsuarioServiceTestMockBean {
	
	static String email = "cleber@email.com";
	static String senha = "senha";
		
	UsuarioService usuarioService;
	
	@MockBean
	UsuarioRepository usuarioRepository;
	
	@Before  
	public void setUp() { 
		// o @MockBean faz isso - usuarioRepository = Mockito.mock(UsuarioRepository.class);
		usuarioService = new UsuarioServiceImpl(usuarioRepository); 
	}
	
	@Test
	public void deveSalvarUmUsuarioNaMemoria() {
		//cenario
	}
	
	@Test(expected = Test.None.class)
	public void deveAutenticarUmUsuarioComSucesso() {
		//cenario		
		Usuario usuario = criarUsuarioNaMemoria();
		Mockito.when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
		
		//ação / execução
		Usuario result = usuarioService.autenticar(email, senha);
		
		//verificação
		Assertions.assertThat(result).isNotNull();
	}
	@Test  //(expected = ErroAutenticacao.class)
	public void deveLancarErroQuandoNaoEncontrarUsuarioComOEmailInformado() {
		//cenario
		Mockito.when(usuarioRepository.findByEmail(Mockito.anyString())).thenReturn(Optional.empty());
		
		//ação / execução
		//...usuarioService.autenticar("cleber@email", "senha") );
		Throwable exception = Assertions.catchThrowable( () -> usuarioService.autenticar(email, senha));
		
		//verificação
		Assertions.assertThat(exception)
		.isInstanceOf(ErroAutenticacao.class)
		.hasMessage("Hun, não existe usuário com o email informado.");
		
	}
	
	@Test
	public void deveLancarErroQuandoASenhaNaoForIgualAAtual() {
		//cenario
		Usuario usuario = criarUsuarioNaMemoria();
		Mockito.when(usuarioRepository.findByEmail(Mockito.anyString())).thenReturn(Optional.of(usuario));
		
		//ação / execução
		//...usuarioService.autenticar("cleber@email", "123456") );
		Throwable exception = Assertions.catchThrowable( () -> usuarioService.autenticar(email, "123456") ); //expressão lambida ()->
		
		//verficação
		Assertions.assertThat(exception)
		.isInstanceOf(ErroAutenticacao.class).hasMessage("Hun, essa senha parece não ser a atual.");
	}
	
	
	@Test(expected = Test.None.class) //não lança exception **
	public void deveValidarEmail() {
		//cenario
		Mockito.when(usuarioRepository.existsByEmail(Mockito.anyString())).thenReturn(false);
		
		//ação / execuçao
		usuarioService.validarEmail("cleber@email.com");
		
		//não vai ser verificado para não retornar erro, vai apenas validar. **
				
	}
	@Test(expected = RegraNegocioException.class)
	public void deveLancarErroAoValidarEmailQuandoExistirEmailCadastradoNaBaseDeDados() {
		
		//cenario
		Mockito.when(usuarioRepository.existsByEmail(Mockito.anyString())).thenReturn(true);
		
		//ação / execução
		usuarioService.validarEmail("email@email.com");
	}
	
	public static Usuario criarUsuarioNaMemoria() {
		return Usuario
				.builder()
				.email(email)
				.senha(senha)
				.id(1L)
				.build();
	
	}
}
