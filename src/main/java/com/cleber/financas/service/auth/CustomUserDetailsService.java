package com.cleber.financas.service.auth;

import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.model.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    /*fazendo a injecao de dependencia via construtor*/
    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository){
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(()
                        -> new UsernameNotFoundException("Verifique o email " + email + " e tente novamente."));
        return (UserDetails) Usuario.builder()
                .email(usuario.getEmail())
                .senha(usuario.getSenha())
                .build();

    }
}
