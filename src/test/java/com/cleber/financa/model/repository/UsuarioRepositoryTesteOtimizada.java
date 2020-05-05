package com.cleber.financa.model.repository;

import java.util.Optional;

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
@DataJpaTest //entity on base,  rollback. repository test
@AutoConfigureTestDatabase(replace = Replace.NONE)  //nao sobrepoe configuração do "test"
public class UsuarioRepositoryTesteOtimizada {
	
	@Autowired
	UsuarioRepository usuarioRepository;
	@Autowired
	TestEntityManager entityManager;

	@Test
	public void deveVerificarAExistenciaDeUmEmailNaBaseDeDados() {
		//cenario
		Usuario usuario = criarUmUsuario();
		entityManager.persist(usuario);
		
		//ação / execução
		boolean result = usuarioRepository.existsByEmail("cleber@email.com");
		
		//verificação
		Assertions.assertThat(result).isTrue();	
		
	} //in this case, rollback

	@Test
	public void deveRetornarFalsoQuandoNaoHouverUsuarioCadastradoComEmailNabaseDeDados() {

		boolean result = usuarioRepository.existsByEmail("eaiMerrmao@email.com");

		Assertions.assertThat(result).isFalse();
	}
	
	@Test
	public void devePersistirUmUsuarioNaBaseDeDados() {
		
		//cenario
		Usuario usuario = criarUmUsuario();
						
		//ação / execução
		//Usuario usuarioSalvo = entityManager.persist(usuario);
		Usuario usuarioSalvo = usuarioRepository.save(usuario);
		
		//verificação
		Assertions.assertThat(usuarioSalvo.getId()).isNotNull();
	}
	
	@Test
	public void deveBuscarnaBaseDeDadosUmUsuarioPorEmail() {
		//cenario
		Usuario usuario = criarUmUsuario();
		entityManager.persist(usuario);
		
		//ação
		Optional<Usuario> result = usuarioRepository.findByEmail("cleber@email.com");
		
		//verificação
		Assertions.assertThat(result.isPresent()).isTrue();
	}
	
	@Test
	public void deveRetonarVazioAoBuscarUsuarioPorEmailQuandoNaoExisteNaBaseDeDados() {
		//nao retona nenhum usuario			
		//ação
		Optional<Usuario> result = usuarioRepository.findByEmail("cleber@email.com");
		
		//verificação
		Assertions.assertThat(result.isPresent()).isFalse();
	}
	
	//metodo para criar usuario
	public static Usuario criarUmUsuario() {
		return Usuario
				.builder()
				.nome("usuario")
				.email("cleber@email.com")
				.senha("senha")
				.build();
		
	}

}
