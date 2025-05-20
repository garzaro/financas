package com.cleber.financas.service.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.cleber.financas.exception.ErroDeAutenticacao;
import com.cleber.financas.exception.RegraDeNegocioException;
import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.model.repository.UsuarioRepository;
import com.cleber.financas.service.UsuarioService;

//@NoArgsConstructor
@Service
public class UsuarioServiceImpl implements UsuarioService {
	
	UsuarioRepository usuarioRepository;
	
	public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {
		super();
		this.usuarioRepository = usuarioRepository;
	}

	@Override
	public Usuario autenticar(String email, String senha) {
		/*validar o login*/
		Optional<Usuario> usuario = usuarioRepository.findByEmail(email);
		/*verificar se o usuario já existe*/
		if (!usuario.isPresent()) {
			throw new ErroDeAutenticacao("Verifique seu usuário e tente novamente");			
		}
		if (!usuario.get().getSenha().equals(senha)) {
			throw new ErroDeAutenticacao("Senha incorreta. tente novamente ou clique em \"Esqueceu a senha?\" para escolher outra");						
		}
		return usuario.get();
	}

	@Override
	public Usuario salvar(Usuario usuario) {
		/*verificar se ja existe email e cpf antes de salvar*/
		validarEmailAndCpf(usuario.getEmail(), usuario.getCpf());
		/*se nao existir nada, salva a instancia*/
		return usuarioRepository.save(usuario);
	}
	
	@Override
	public void validarEmailAndCpf(String email, String cpf) {
		/*ver se email e cpof existe*/
		boolean existeUsuarioComEsseEmail = usuarioRepository.existsByEmail(email);
		boolean existeUsuarioComEsseCpf = usuarioRepository.existsByCpf(cpf);
		
		if (existeUsuarioComEsseEmail) {
			throw new RegraDeNegocioException("Ja existe um usuário com esse email");
		}
		if (existeUsuarioComEsseCpf) {
			throw new RegraDeNegocioException("Ja existe um usuário com esse cpf");
		}		
	}
	
	/*public void senhaCriptografada(Usuario usuario) {
	String pegarSenha = usuario.getSenha();
	String criptografar = encoder.encode(pegarSenha);
	usuario.setSenha(criptografar);
}*/

	@Override
	public Optional<Usuario> pegarUsuarioPorCpf(String cpf) {
		return usuarioRepository.findByCpf(cpf);
	}

	@Override
	public Optional<Usuario> obterUsuarioPorId(Long id) {
		return usuarioRepository.findById(id);
	}	
}