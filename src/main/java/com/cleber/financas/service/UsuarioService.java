package com.cleber.financas.service;

import com.cleber.financas.model.entity.Usuario;

import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public interface UsuarioService {
    /*verificar se o usuario existe na base, validação*/
    Usuario autenticar(String email, String senha);

    /*salvar o usuario na base*/
    Usuario salvar(Usuario usuario);

    /*verifica o email ou CPF existe na base de dados, unique */
    void validarEmailAndCpf(String email, String cpf);
    
    /*pegar usuario por cpf*/
    Optional<Usuario> pegarUsuarioPorCpf(String cpf);

    Optional<Usuario> obterUsuarioPorId(Long id);
}
