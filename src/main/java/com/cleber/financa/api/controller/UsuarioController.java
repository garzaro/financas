package com.cleber.financa.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.cleber.financa.api.dto.UsuarioDTO;
import com.cleber.financa.model.entity.Usuario;
import com.cleber.financa.service.UsuarioService;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
	
	UsuarioService usuarioService;

	public UsuarioController(UsuarioService usuarioService) {
		this.usuarioService = usuarioService;
	}
	
	public ResponseEntity salvar(@ResponseBody UsuarioDTO dto) {
		
		Usuario usuario = criarEntidadeUsuario();
		
	}
		public static Usuario criarEntidadeUsuario(){
		return null; //será criado
		
	}
}

