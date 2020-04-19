package com.cleber.financa.model.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.cleber.financa.model.entity.Usuario;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest //entity on base,  rollback.
@AutoConfigureTestDatabase(replace = Replace.NONE)  //nao sobrepoe configuração do "test"
public class UsuarioRepositoryTesteOtimizada {
	
	@Autowired
	UsuarioRepository usuarioRepository;
	@Autowired
	TestEntityManager entityManager;

	@Test
	public void deveVerificarAExistenciaDeUmEmailNaBaseDeDados() {
		
		Usuario usuario = Usuario.builder().nome("usuario").email("usuario@gmail.com").build();
		entityManager.persist(usuario);

		boolean result = usuarioRepository.existsByEmail("usuario@gmail.com");

		Assertions.assertThat(result).isTrue();

	} //in this case, rollback

	@Test
	public void deveRetornarFalsoQuandoNaoHouverUsuarioCadastradoComEmailNabaseDeDados() {

		boolean result = usuarioRepository.existsByEmail("carai@gmail.com");

		Assertions.assertThat(result).isFalse();
	}

}
