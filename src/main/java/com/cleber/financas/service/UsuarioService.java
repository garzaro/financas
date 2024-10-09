package com.cleber.financas.service;

import com.cleber.financas.model.entity.Usuario;

import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public interface UsuarioService {
    /*verificar se o usuario existe na base, validação*/
    Usuario autenticarUsuario(String email, String senha);

    /*salvar o usuario na base*/
    Usuario persistirUsuarioNabaseDeDados(Usuario usuario);

    /*verifica o email ou CPF existe na base de dados, unique */
    void validarEmailAndCadastroPessoaFisicaNaBaseDedados(String email, String cadastroPessoaFisica);

    Optional<Usuario> obterUsuarioPorId(Long id);
}
