package com.cleber.financa.service;

import org.springframework.stereotype.Service;

import com.cleber.financa.model.entity.Usuario;

@Service
public interface UsuarioService {
	
	Usuario autenticar (String email, String senha);
	
	Usuario salvarUsuario(Usuario usuario);
	
	void validarEmail(String email);	

}
