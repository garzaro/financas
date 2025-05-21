package com.cleber.financas.service;

import com.cleber.financas.model.entity.Usuario;

import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public interface UsuarioService {
    /*verificar se o usuario existe na base, validação*/
    Usuario autenticar(String email, String senha);

    /*salvar o usuario na base*/
    Usuario salvarUsuario(Usuario usuario);
    
    /*unique*/
    void validarEmail(String email);
    
    /*unique */
    void validarCpf(String cpf);
    
    Optional<Usuario> obterUsuarioPorId(Long id);
    
    Optional<Usuario> obterUsuarioPorCpf(String cpf);   
}
