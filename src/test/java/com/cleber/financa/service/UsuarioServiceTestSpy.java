package com.cleber.financa.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.cleber.financa.exceptions.RegraNegocioException;
import com.cleber.financa.model.entity.Usuario;
import com.cleber.financa.model.repository.UsuarioRepository;
import com.cleber.financa.service.impl.UsuarioServiceImpl;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class UsuarioServiceTestSpy {
	
	@SpyBean
	UsuarioServiceImpl usuarioServiceImpl;
	
	@MockBean
	UsuarioRepository usuarioRepository;
	
	//Validar email e salvar usuario
	
	@Test(expected = Test.None.class) 
	public void deveSalvarUmUsuarioNaMemoria() {
		//cenario
		Mockito.doNothing().when(usuarioServiceImpl).validarEmail(Mockito.anyString());
		Usuario usuario = criarUsuarioNaMemoria();
		Mockito.when(usuarioRepository.save(Mockito.any(Usuario.class))).thenReturn(usuario);
		
		//ação / execução
		Usuario usuarioSalvo = usuarioServiceImpl.salvarUsuario(new Usuario());		
		
		//veridicação
		Assertions.assertThat(usuarioSalvo).isNotNull();
		Assertions.assertThat(usuarioSalvo.getId()).isEqualTo(1l);
		Assertions.assertThat(usuarioSalvo.getNome()).isEqualTo(usuario.getNome());
		Assertions.assertThat(usuarioSalvo.getEmail()).isEqualTo(usuario.getEmail());
		Assertions.assertThat(usuarioSalvo.getSenha()).isEqualTo(usuario.getSenha());
		//Assertions.assertThat(usuarioSalvo.getSenha()).isEqualTo("senha");
			
	}
	
	@Test(expected = RegraNegocioException.class)
	public void naoDeveSalvarUmUsuarioComEmailJaCadastrado() {
		//cenario
		Usuario usuario = criarEmail();
		Mockito.doThrow(RegraNegocioException.class).when(usuarioServiceImpl).validarEmail(criarEmail().getEmail());
		
		//ação / execução
		usuarioServiceImpl.salvarUsuario(usuario);
		
		//verificação
		Mockito.verify(usuarioRepository, Mockito.never()).save(usuario); //nunca chama o metodo save.
		
	}
	
	public static Usuario criarEmail() {
		return Usuario
				.builder()
				.email("cleber@email.com")
				.build();
	}

	public static Usuario criarUsuarioNaMemoria() {
		
		return Usuario
				.builder()
				.id(1l)
				.nome("cleber")
				.email("cleber@email.com")
				.build();
	}
	

}	
	
	














	
	
	//	Abaixo um metodo para comparar a diferença de mock com o uso do spy
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
//	public static Usuario criarUsuarioNaBase() {
//		return Usuario
//				.builder()
//				.nome("cleber")
//				.email("cleber@email.com")
//				.build();
//		
//	}

//}
