package com.cleber.financas.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.cleber.financas.model.entity.Usuario;

@Service
public interface UsuarioService {

    /*verificar se o usuario existe na base, validação*/
    Usuario autenticar(String email, String senha);

    /**
     * Registro simplificado via fluxo JWT (nome + email + senha).
     * Verifica duplicidade de email e encodifica a senha antes de persistir.
     */
//    Usuario registrar(RegisterRequest request);

    /*salvar o usuario na base (fluxo completo com CPF)*/
    Usuario salvarUsuario(Usuario usuario);

    /*atualizar o usuario*/
    Usuario atualizarUsuario(Usuario usuario);

    /**validar usuario*/
    void validarUsuario(Usuario usuario);

    /*ver se existe o email*/
    void validarEmailCpf(String email, String cpf);

    Optional<Usuario> obterUsuarioPorId(UUID uuid);

    Optional<Usuario> obterUsuarioPorCpf(String cpf);
}
