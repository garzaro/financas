package com.cleber.financa.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.cleber.financa.exceptions.RegraNegocioException;
import com.cleber.financa.model.repository.UsuarioRepository;
import com.cleber.financa.service.impl.UsuarioServiceImpl;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class UsuarioServiceTestMock {
	//para testes unitarios não é necessario injetar as instancias oficiais.
	
	UsuarioService usuarioService;
	UsuarioRepository usuarioRepository;
	
	/* @Before - será executado antes dos demais testes desta classe
	 * setUp() {} - metodo para configurar os testes
	 */
	@Before  
	public void setUp() { 
		usuarioRepository = Mockito.mock(UsuarioRepository.class); //do fake call of repository methods
		usuarioService = new UsuarioServiceImpl(usuarioRepository); //call real method of service
	}
	
	@Test(expected = Test.None.class) //não lança exception **
	public void deveValidarEmail() {
		//cenario
		Mockito.when(usuarioRepository.existsByEmail(Mockito.anyString())).thenReturn(false);
		
		//ação / execuçao
		usuarioService.validarEmail("cleber@email.com");
		
		//não vai ser verificado para não retornar erro, apenas validar. **
				
	}
	@Test(expected = RegraNegocioException.class)
	public void deveLancarErroAoValidarEmailQuandoExistirEmailCadastradoNaBaseDeDados() {
		
		//cenario
		Mockito.when(usuarioRepository.existsByEmail(Mockito.anyString())).thenReturn(true);
		
		//ação / execução
		usuarioService.validarEmail("email@email.com");
	}
}
