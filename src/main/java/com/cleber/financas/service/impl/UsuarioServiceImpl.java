package com.cleber.financas.service.impl;

import com.cleber.financas.config.PasswordEncoderConfig;
import com.cleber.financas.exception.ErroDeAutenticacao;
import com.cleber.financas.exception.RegraDeNegocioException;
import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.model.repository.UsuarioRepository;
import com.cleber.financas.service.UsuarioService;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import jakarta.transaction.Transactional;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.beans.Encoder;
import java.util.Optional;

/* caso tenhamos que usa o bcrypt private PasswordEncoder passwordEncoder;*/
//@NoArgsConstructor
@Service
public class UsuarioServiceImpl implements UsuarioService {
    @Autowired
    UsuarioRepository usuarioRepository;
    @Autowired
    PasswordEncoderConfig passwordEncoder;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {
        super();
        this.usuarioRepository = usuarioRepository;
    }
    
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
        /*hash da senha antes de salva a instancia*/
        String hashSenha = passwordEncoder.encode(usuario.getSenha());
        usuario.setSenha(hashSenha);
        /*se nao existir email e nem cpf, salva a instancia com o hash da senha*/
        Usuario usuarioSalvo = usuarioRepository.save(usuario);
        /*para remover a senha do objeto retornado para evitar vazamento*/
        usuarioSalvo.setSenha(null);
        return usuarioSalvo;

    }

    public void validacao(String email, String cpf){
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
        /*ver se o cpf existe*/
        boolean existeUsuarioComCpf = usuarioRepository.existsByCpf(cpf);

        if (existeUsuarioComCpf) {
            throw new RegraDeNegocioException("Já existe um usuário com esse CPF");
        }
    }

    /*public void senhaCriptografada(Usuario usuario) {
    	String pegarSenha = usuario.getSenha();
    	String criptografar = encoder.encode(pegarSenha);
    	usuario.setSenha(criptografar);
    }*/

    @Override
    public Optional<Usuario> obterUsuarioPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    @Override
    public Optional<Usuario> obterUsuarioPorCpf(String cpf) {
        return  usuarioRepository.findByCpf(cpf);
    }
}
