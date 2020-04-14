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

	@Autowired
	UsuarioRepository userRepository;

	/* Para iniciar o teste são necessario tres elemento */
	@Test
	public void deveVerificarAExistenciaDeUmEmail() {

		/* cenario */

		Usuario usuario = Usuario.builder().nome("usuario").email("usuario@gmail.com").build();
		userRepository.save(usuario);

		/* ação / a excução do teste */
		boolean result = userRepository.existsByEmail("usuario@gmail.com");
		boolean results = userRepository.existsByNome("usuario");

		/* verificação */
		Assertions.assertThat(result).isTrue();
		Assertions.assertThat(results).isTrue();

	}

}
