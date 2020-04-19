package com.cleber.financa.model.repository;

import org.assertj.core.api.Assertions;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import com.cleber.financa.model.entity.Usuario;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class UsuarioRepositoryTest {
	
	/* Aqui estamos criando o cenario de teste com a propria
	 * classe que estamos testando o que é certo mas nao e totalmente
	 * recomendado
	 * Por favor, classe de teste otimizada
	 * 
	 * */

	@Autowired
	UsuarioRepository usuarioRepository; //testar o usuarioRepository

	/* Para iniciar o teste são necessario tres elemento */
	@Test
	public void deveVerificarAExistenciaDeUmEmail() {

		/* cenario */
		usuarioRepository.deleteAll();
		Usuario usuario = Usuario.builder().nome("usuario").email("usuario@gmail.com").build();
		usuarioRepository.save(usuario);

		/* ação / a excução do teste */
		boolean result = usuarioRepository.existsByEmail("usuario@gmail.com");
		boolean results = usuarioRepository.existsByNome("usuario");

		/* verificação */
		Assertions.assertThat(result).isTrue();
		Assertions.assertThat(results).isTrue();

	}
	@Test
	public void deveRetornarFalsoQuandoNaoHouverUsuarioCadastradoComEmail() {
		
		//cenario
		usuarioRepository.deleteAll(); //garante a base limpa
		
		//ação / execução
		boolean result = usuarioRepository.existsByEmail("usuario@gmail.com");
		
		//verificação
		Assertions.assertThat(result).isFalse();
	}

}
