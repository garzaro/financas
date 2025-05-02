package com.cleber.financas.service.impl;

import com.cleber.financas.exception.ErroDeAutenticacao;
import com.cleber.financas.exception.RegraDeNegocioException;
import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.model.repository.UsuarioRepository;
import com.cleber.financas.service.UsuarioService;
import jakarta.transaction.Transactional;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.beans.Encoder;
import java.util.Optional;

//@NoArgsConstructor
@Service
public class UsuarioServiceImpl implements UsuarioService {
    
    UsuarioRepository usuarioRepository;
    
    public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {
        super();
        this.usuarioRepository = usuarioRepository;
    }
    
    @Override
    public Usuario autenticarUsuario(String email, String senha) {
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
    public Usuario persistirUsuarioNabaseDeDados(Usuario usuario) {
        /*deve validar o email e o cpf, verificar se existe*/
        validarEmailAndCadastroPessoaFisicaNaBaseDedados(
                usuario.getEmail(), usuario.getCadastroPessoaFisica());
        /*se nao existir email, salva a instancia*/
        return usuarioRepository.save(usuario);
    }
    
    /*public void senhaCriptografada(Usuario usuario) {
    	String pegarSenha = usuario.getSenha();
    	String criptografar = encoder.encode(pegarSenha);
    	usuario.setSenha(criptografar);
    }*/
    
    @Override
    public void validarEmailAndCadastroPessoaFisicaNaBaseDedados(String email, String cadastroPessoaFisica) {
        /*ver se o email existe*/
        boolean existeUsuarioComEmail = usuarioRepository.existsByEmail(email);
        boolean existeUsuarioComCpf = usuarioRepository.existsByCadastroPessoaFisica(cadastroPessoaFisica);
        
        if (existeUsuarioComEmail) {
            throw new RegraDeNegocioException("Já existe um usuário com esse email.");
        }
        if (existeUsuarioComCpf) {
            throw new RegraDeNegocioException("Já existe um usuário com esse CPF");
        }
        
    }
    
    @Override
    public Optional<Usuario> obterUsuarioPorId(Long id) {
        return usuarioRepository.findById(id);
    }
}
