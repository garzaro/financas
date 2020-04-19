package com.cleber.financa.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cleber.financa.exceptions.ErroAutenticacao;
import com.cleber.financa.exceptions.RegraNegocioException;
import com.cleber.financa.model.entity.Usuario;
import com.cleber.financa.model.repository.UsuarioRepository;
import com.cleber.financa.service.UsuarioService;

import lombok.Builder;

@Builder
@Service
public class UsuarioServiceImpl implements UsuarioService {
	
	/*UsuarioService nao pode acessar direto a base então usa o repository*/
	
	private UsuarioRepository usuarioRepository;
	
	/*Metodo construtor para receber essa implementação REPOSITORY como parametro
	 * 
	 * Para que o Impl funcione é necessario construi lo com instancia de UserRepository
	 * 
	 * */
	@Autowired
	public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {
		super();
		this.usuarioRepository = usuarioRepository;
	}	

	@Override
	public Usuario autenticar(String email, String senha) {
		
		Optional<Usuario> usuario = usuarioRepository.findByEmail(email);
		
		if(!usuario.isPresent()) {
			throw new ErroAutenticacao("Hun, não existe usuário com o email informado.");
			
		}
		
		if(!usuario.get().getSenha().equals(senha)) {
			throw new ErroAutenticacao("Hun, essa senha parece não ser a correta.");
			
		}
				
		return usuario.get();		
	}

	@Override
	@Transactional
	public Usuario salvarUsuario(Usuario usuario) {
		validarEmail(usuario.getEmail());
		return usuarioRepository.save(usuario);
	}

	@Override
	public void validarEmail(String email) {
		boolean existe = usuarioRepository.existsByEmail(email);
		if (existe) {
			throw new RegraNegocioException("Já existe usuário cadastrado com esse email");
			
			
		}
		
	}

}
