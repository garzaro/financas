package com.cleber.financa.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cleber.financa.api.dto.UsuarioDTO;
import com.cleber.financa.exceptions.ErroAutenticacao;
import com.cleber.financa.exceptions.RegraNegocioException;
import com.cleber.financa.model.entity.Usuario;
import com.cleber.financa.service.UsuarioService;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

	UsuarioService usuarioSerice;

	// Contrutor para injeção de dependencia
	
	public UsuarioController(UsuarioService usuarioService) {
		this.usuarioSerice = usuarioService;
	}
	
	//Autenticação de usuario
	
	@PostMapping("/auth")
	public ResponseEntity autenticar(@RequestBody UsuarioDTO dto) {

		try {
			Usuario autenticacaoUsuario = usuarioSerice.autenticar(dto.getEmail(), dto.getSenha());
			return ResponseEntity.ok(autenticacaoUsuario);

		} catch (ErroAutenticacao e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}

	}

//	ResponseEntity - Objeto que irá representar o corpo da resposta.
//	dto transformado em entidade Usuario

	@PostMapping
	public ResponseEntity salvar(@RequestBody UsuarioDTO dto) {

		// dto transformado em entidade usuario
		Usuario usuario = Usuario.builder().nome(dto.getNome()).email(dto.getEmail()).senha(dto.getSenha()).build();
		try {
			Usuario usuarioSalvo = usuarioSerice.salvarUsuario(usuario);
			return new ResponseEntity(usuarioSalvo, HttpStatus.CREATED);
		} catch (RegraNegocioException e) {
			return ResponseEntity.badRequest().body(e.getMessage());

		}

	}

}