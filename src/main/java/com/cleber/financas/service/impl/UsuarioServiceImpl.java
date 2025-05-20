package com.cleber.financas.service.impl;

import com.cleber.financas.exception.ErroDeAutenticacao;
import com.cleber.financas.exception.RegraDeNegocioException;
import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.model.repository.UsuarioRepository;
import com.cleber.financas.service.UsuarioService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

//@NoArgsConstructor
@Service
public class UsuarioServiceImpl implements UsuarioService {
    
    private UsuarioRepository usuarioRepository;
    
    public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {
        super();
        this.usuarioRepository = usuarioRepository;
    }
    /*lista de email permitidos*/
    private static final List<String> dominiosEmaislPermitidos = List.of(
            "gmail.com", "yahoo.com", "hotmail.com", "outlook.com", "edu.br", "org", "gov.br"
    );
    /*login: validação e autenticação*/
    @Override
    public Usuario autenticar(String email, String senha) {
        /*login, validando login*/
        Optional<Usuario> usuario = usuarioRepository.findByEmail(email);
        /*verificar a existencia de usuario na base de dados*/
        if (!usuario.isPresent()) {
            throw new ErroDeAutenticacao("Verifique seu usuário e tente novamente.");
        }
        if (!usuario.get().getSenha().equals(senha)) {
            throw new ErroDeAutenticacao("Senha incorreta. Tente novamente ou clique em \"Esqueceu a senha?\" para escolher outra.");
        }
        return usuario.get();
    }
    
    @Override
    @Transactional
    public Usuario salvarUsuario(Usuario usuario) {
        /*deve validar o email e o cpf, verificar se existe*/
        validacao(usuario.getEmail(), usuario.getCpf());
        /*se nao existir email, salva a instancia*/
        return usuarioRepository.save(usuario);
    }  

    @Override
    public Optional<Usuario> obterUsuarioPorId(Long id) {
        return usuarioRepository.findById(id);
    }

	@Override
	public Optional<Usuario> obterUsuarioPorCpf(String cpf) {
		return usuarioRepository.findByCpf(cpf);
	}
    
    /*public void senhaCriptografada(Usuario usuario) {
    	String pegarSenha = usuario.getSenha();
    	String criptografar = encoder.encode(pegarSenha);
    	usuario.setSenha(criptografar);
    }*/
	
	public void validacao(String email, String cpf) {
		validarEmail(email);
		validarCpf(cpf);
	}	
    
    @Override
    public void validarEmail(String email) {
        /*ver se o email existe*/
        boolean existeUsuarioComEsseEmail = usuarioRepository.existsByEmail(email);       
        if (existeUsuarioComEsseEmail) {
            throw new RegraDeNegocioException("Já existe um usuário com esse email");
        }  
    }
	
	@Override
	public void validarCpf(String cpf) {
		/*ver se existe o cpf*/
		boolean existeUsuarioComEsseCpf = usuarioRepository.existsByCpf(cpf);
		if (existeUsuarioComEsseCpf) {
            throw new RegraDeNegocioException("Já existe um usuário com esse CPF");
        }		
	}   
}
