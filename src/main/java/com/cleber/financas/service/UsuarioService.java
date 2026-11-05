package com.cleber.financas.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.cleber.financas.api.dto.AutenticacaoResultado;
import com.cleber.financas.api.dto.RequestLoginDTO;
import com.cleber.financas.model.entity.Usuario;

@Service
public interface UsuarioService {

    /* autenticar usuario via request e emitir access + refresh tokens */
    AutenticacaoResultado autenticar(RequestLoginDTO request, String ip, String agenteUsuario);

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

    /* trocar a senha do usuario autenticado conferindo a senha atual */
    void trocarSenha(UUID usuarioId, String senhaAtual, String novaSenha);

    /* metodo comum para codificar nova senha, persistir e revogar todas as sessoes */
    void alterarSenha(UUID usuarioId, String novaSenha);
}
