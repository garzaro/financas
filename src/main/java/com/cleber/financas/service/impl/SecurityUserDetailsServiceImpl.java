package com.cleber.financas.service.impl;


import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.model.repository.UsuarioRepository;
import com.cleber.financas.security.UsuarioAutenticado;

/**
 * TODO-list
 * [] Conta desabilitada
 * [] Usuario !isAtivo
 * **/


@Service
public class SecurityUserDetailsServiceImpl implements UserDetailsService {

    private UsuarioRepository usuarioRepository;

    public SecurityUserDetailsServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
    * aqui eu posso buscar o usuario numa planilha ou um bloco de texto
    * e nao somente no bancode dados
    * **/
    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        Usuario usuarioEncontrado = usuarioRepository
                .findByEmail(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("usuário não encontrado!!!"));
//        if (usuarioEncontrado == null) {
//            throw new UsernameNotFoundException("username " + username + " is not found");
//        }
        /**deve retornar aviso de usuario disable aqui - ver conta expira depois**/
        return new UsuarioAutenticado(
                usuarioEncontrado.getUuid(),
                usuarioEncontrado.getEmail(),
                usuarioEncontrado.getSenha(),
                Boolean.TRUE.equals(usuarioEncontrado.getIsAtivo()),
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }
}
