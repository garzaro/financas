package com.cleber.financa.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.cleber.financa.model.repository.UsuarioRepository;
import com.cleber.financa.service.impl.UsuarioServiceImpl;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class UsuarioServiceTestSpy {
	
	@SpyBean
	UsuarioServiceImpl usuarioServiceImpl;
	
	@MockBean
	UsuarioRepository usuarioRepository;
	
	@Test
	public void deveSalvarUmUsuarionaBaseDeDados() {
		//cenario
		Mockito.doNothing().when(usuarioServiceImpl).validarEmail(Mockito.anyString());
		
	}
	
//	Abaixo um metodo para compara a diferença de mock com o uso do spy
//	
//	@Tes(expected = Test.None.class)
//	public void deveAutenticarUmUsuarioComSucesso() {
//		//cenario		
//		Usuario usuario = criarUsuarioNaBase();
//		Mockito.when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
//		
//		//ação / execução
//		Usuario result = usuarioService.autenticar(email, senha);
//		
//		//verificação
//		Assertions.assertThat(result).isNotNull();
//	}

}
