package com.cleber.financa.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cleber.financa.exceptions.RegraNegocioException;
import com.cleber.financa.model.entity.Usuario;
import com.cleber.financa.model.repository.UsuarioRepository;
import com.cleber.financa.service.UsuarioService;

import lombok.Builder;

@Builder
@Service
public class UsuarioServiceImpl implements UsuarioService {
	
	/*UsuarioService nao pode acessar direto a base então usa o repository*/
	
	private UsuarioRepository userRepository;
	
	/*Metodo construtor para receber essa implementação REPOSITORY como parametro
	 * 
	 * Para que o Impl funcione é necessario construi lo com instancia de UserRepository
	 * 
	 * */
	@Autowired
	public UsuarioServiceImpl(UsuarioRepository repository) {
		super();
		this.userRepository = repository;
	}	

	@Override
	public Usuario autenticar(String email, String senha) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Usuario salvarUsuario(Usuario usuario) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void validarEmail(String email) {
		boolean existe = userRepository.existsByEmail(email);
		if (existe) {
			throw new RegraNegocioException("Já existe usuário cadastrado com esse email");
			
			
		}
		
	}

}
