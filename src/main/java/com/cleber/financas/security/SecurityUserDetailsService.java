package com.cleber.financas.security;

import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.model.repository.UsuarioRepository;

import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Faz a ponte entre o {@link UserDetailsService} do Spring Security e o
 * armazenamento de usuários do JPA.
 * É chamado durante a autenticação para carregar as credenciais do usuário e as
 * autoridades concedidas.
 */

@Service
public class SecurityUserDetailsService implements UserDetailsService {
    /** fazendo a injecao de dependencia via construtor **/
    private UsuarioRepository usuarioRepository;

    public SecurityUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuarioRequest = usuarioRepository.findByEmail(email) // retorna optional
                .orElseThrow(() -> new UsernameNotFoundException("Verifique o email " + email + " e tente novamente."));
        /**
         * transforma o usuario de forma que o Spring Security entenda como detalhes do usuario
         * especialização da classe User par corrigir o erro de conflito de ids com
         * outros usuarios que estão no banco. USando este modelo deve se habilitar o uso de @PreAuthorize no controller
         **/
        // return new UsuarioAutenticado(
        //         usuarioRequest.getId(),
        //         usuarioRequest.getEmail(),
        //         usuarioRequest.getSenha(),
        //         AuthorityUtils.createAuthorityList("ROLE_USER"));

        // Deixando um exemplo aqui de como poderia ser se fosse pra retornar um usuario
        // padrao do spring security
        return User.builder()
        .username(usuarioRequest.getEmail())
        .password(usuarioRequest.getSenha())
        // .roles("USER") //fica com a role padrao
        // .authorities(null)
        // .accountExpired(false)
        // .accountLocked(false)
        // .credentialsExpired(false)
        // .disabled(false)
        .build();
    }
}

/**
 * UserDetailsService
 * 
 * Vai na base e pega o username, no nosso caso o email, o proprio spring
 * security passa isso pra cá
 * 
 * Esse cara é apenas um buscador, ele nao valida nada, nem se o usuario está
 * ativo nem nada.
 * Aqui nao se verifica a senha, quem faz isso é o AutenticationManager
 * 
 * Esse cara pode esta coletando usaurio de qualquer repositorio, pode ser no
 * banco ou em uma lista em uma planilha ou arquivo de texto
 * 
 **/
