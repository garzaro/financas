package com.cleber.financas.service;

import com.cleber.financas.model.entity.Usuario;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
public interface UsuarioService {
    /*verificar se o usuario existe na base, validação*/
    Usuario autenticar(String email, String senha);

    /*salvar o usuario na base*/
    Usuario salvarUsuario(Usuario usuario);

    /*atualizar o usuario*/
    Usuario atualizarUsuario(Usuario usuario);

    /**validar usuario*/
    void validarUsuario(Usuario usuario);
    
    /*ver se existe o email*/
    void validarEmailCpf(String email, String cpf);
    
    Optional<Usuario> obterUsuarioPorId(Long id);
    
    Optional<Usuario> obterUsuarioPorCpf(String cpf);   
}
