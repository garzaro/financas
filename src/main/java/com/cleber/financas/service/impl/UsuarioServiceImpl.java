package com.cleber.financas.service.impl;

import com.cleber.financas.exception.ErroDeValidacao;
import com.cleber.financas.exception.RegraDeNegocioException;
import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.model.repository.UsuarioRepository;
import com.cleber.financas.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
public class UsuarioServiceImpl implements UsuarioService {
    @Autowired
    UsuarioRepository usuarioRepository;
    
    public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {
        super();
        this.usuarioRepository = usuarioRepository;
    }
    
    @Override
    public Usuario autenticarUsuario(String email, String senha) {
        /*login*/
        Optional<Usuario> validandoLogin = usuarioRepository.findByEmail(email);
        /*verificar a existencia de usuario na base de dados*/
        if (!validandoLogin.isPresent()){
            throw new ErroDeValidacao("Usuario não encontrado pelo email informado");
        }
        return validandoLogin.get();
    }
    
    @Override
    public Usuario persistirUsuarioNabaseDeDados(Usuario usuario) {
        /*deve validar o email, verificar se existe. (metodo do Service)*/
        validarEmailNaBaseDedados(usuario.getEmail());
        /*se nao existir email, salva a instancia*/
        return usuarioRepository.save(usuario);
    }
    
    @Override
    public void validarEmailNaBaseDedados(String email) {
        /*ver se o email existe*/
        boolean verificarSeOEmailExisteNaBaseDeDados = usuarioRepository.existsByEmail(email);
        if (verificarSeOEmailExisteNaBaseDeDados){
            throw new RegraDeNegocioException("Ja existe um usuario com esse email.");
        }
    
    }
}
