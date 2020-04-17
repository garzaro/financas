package com.cleber.financa.service;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import org.springframework.test.context.junit4.SpringRunner;

import com.cleber.financa.exceptions.RegraNegocioException;
import com.cleber.financa.model.entity.Usuario;
import com.cleber.financa.model.repository.UsuarioRepository;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class UsuarioServiceTest {
	
	@Autowired
	UsuarioService usuarioService;
	
	UsuarioRepository usuarioRepository;
	
	@Test(expected = Test.None.class) //não lança exception **
	public void deveValidarEmail() {
		//cenario
		//usuarioRepository.deleteAll();
		
		//ação / execuçao
		usuarioService.validarEmail("cleber@gmail.com");
		
		//não vai ser verificado para não retornar erro, apenas validar. **
				
	}
	@Test(expected = RegraNegocioException.class)
	public void deveLancarErroAoValidarEmailQuandoExistirEmailCadastrado() {
		
		//cenario
		Usuario usuario = Usuario.builder().nome("usuario").email("email@gmail.com").build();
		usuarioRepository.save(usuario);
		
		//ação / execução
		usuarioService.validarEmail("email@email.com");
	}

}
