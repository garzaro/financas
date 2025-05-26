package com.cleber.financas.service.impl;

import com.cleber.financas.exception.ErroDeAutenticacao;
import com.cleber.financas.exception.ErroValidacaoException;
import com.cleber.financas.exception.RegraDeNegocioException;
import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.model.repository.UsuarioRepository;
import com.cleber.financas.service.UsuarioService;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/*endpoints*/
//@NoArgsConstructor
@Service
@Validated
public class UsuarioServiceImpl implements UsuarioService {
    /*injecao por construtor*/
    UsuarioRepository usuarioRepository;
        
    public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }
    /* lista de emails permitidos */
    private static final List<String> dominiosEmailPermitidos = List.of(
            "gmail.com", "edu.br", "gov.br"
    );

    /*login, validação e autenticação*/
    @Override
    public Usuario autenticar(String email, String senha) {
        /*login, validando login*/
        Optional<Usuario> usuario = usuarioRepository.findByEmail(email);
        /*verificar a existencia de usuario na base de dados*/
        if (!usuario.isPresent()) {
            throw new ErroDeAutenticacao("Verifique seu email e tente novamente.");
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
        validarUsuario(usuario);
        /*se nao existir email e nem cpf, salva a instancia com o hash da senha*/
        return usuarioRepository.save(usuario);
    }

    @Override
    public Usuario atualizarUsuario(Usuario usuario) {
        Objects.requireNonNull(usuario.getId());
        validarUsuario(usuario);
        return usuarioRepository.save(usuario);
    }  

    /*VALIDAÇÃO*/
    @Override
    public void validarUsuario(Usuario usuario) {
        /*campos vazio*/
    	validarNome(usuario);
        validarCpf(usuario);
        validarNomeUsuario(usuario);
        validarEmail(usuario);
        /*validacao de dupliciadade*/
        validarEmailCpf(usuario.getEmail(), usuario.getCpf());        
    }
    
    /*validação de existencia*/    
    public void validarEmailCpf(String email, String cpf) {
        /*ver se o email existe*/
        boolean existeUsuarioComEsseEmail = usuarioRepository.existsByEmail(email);
        if (existeUsuarioComEsseEmail) {
            throw new RegraDeNegocioException("Esse email já está em uso");
        }
        /*ver se o cpf existe*/
        boolean existeUsuarioComEsseCpf = usuarioRepository.existsByCpf(cpf);
        if (existeUsuarioComEsseCpf) {
        	throw new RegraDeNegocioException("Já existe usuário com esse CPF");
        }
    }
    
    /*validar nome completo*/
    public void validarNome(Usuario usuario){
        //System.out.println("Nome do usuário: " + usuario.getNome());
        if (usuario.getNome() == null || usuario.getNome().trim().equals("")) {
            throw new ErroValidacaoException("O nome completo é obrigatório");
        }
        if (!Pattern.matches("^[a-zA-Z\\s]+$", usuario.getNome())) {
            throw new ErroValidacaoException("O nome completo deve conter apenas letras e espaços");
        }
    }
    
    /*validar cpf - campo vazio*/
    public void validarCpf(Usuario usuario){
        if (usuario.getCpf() == null || usuario.getCpf().trim().equals("")) {
            throw new ErroValidacaoException("O CPF é obrigatório");
        }
        
    }
   
    /*validar email*/
    public void validarEmail(Usuario usuario){
        if (usuario.getEmail() == null || usuario.getEmail().trim().equals("")) {
            throw new ErroValidacaoException("O email é obrigatório");
        }
        /*validação manual*/
        if (!Pattern.matches("^[\\w-\\.]+@[\\w-\\.]+\\.[a-z]{2,}$", usuario.getEmail())) {
            throw new ErroValidacaoException("O email deve seguir o padrao email@seudominio.com");
        }
        String emailPermitido = usuario.getEmail().substring(usuario.getEmail().lastIndexOf("@") + 1);
        if (!dominiosEmailPermitidos.contains(emailPermitido)) { /*garante que o dominio extraido esteja na lista*/
            throw new ErroValidacaoException("Emails permitidos no cadastro : " + dominiosEmailPermitidos);
        }
    }
    
    /*validar nome usuario*/
    public void validarNomeUsuario(Usuario usuario){
        if (usuario.getUsuario() == null || usuario.getUsuario().trim().equals("")) {
            throw new ErroValidacaoException("O nome de usuário é obrigatório");
        }
    }
    
    /*validar senha - campo vazio*/
    public void validarSenha(Usuario usuario) {
    	if (usuario.getSenha() == null || usuario.getSenha().trim().equals("")) {
    		throw new ErroValidacaoException("informe a senha.");
    	}    	
    }
    
    @Override
    public Optional<Usuario> obterUsuarioPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    @Override
    public Optional<Usuario> obterUsuarioPorCpf(String cpf) {
        return  usuarioRepository.findByCpf(cpf);
    }

    /*public void senhaCriptografada(Usuario usuario) {
    	String pegarSenha = usuario.getSenha();
    	String criptografar = encoder.encode(pegarSenha);
    	usuario.setSenha(criptografar);
    }*/

}
