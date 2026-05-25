package com.cleber.financas.service.impl.auth;

import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.model.repository.UsuarioRepository;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class SecurityUserDetailsService implements UserDetailsService {
    /**fazendo a injecao de dependencia via construtor**/
	private UsuarioRepository usuarioRepository;

	public SecurityUserDetailsService(UsuarioRepository usuarioRepository) {
		this.usuarioRepository = usuarioRepository;
}

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuarioEncontrado = usuarioRepository.findByEmail(email) //retorna optional
                .orElseThrow(()
                        -> new UsernameNotFoundException("Verifique o email " + email + " e tente novamente."));
        
        return User.builder()
        		.username(usuarioEncontrado.getEmail())
        		.password(usuarioEncontrado.getSenha())
        		.roles("USER") //fica com a role padrao
        		.build();
        		          
    }
    
    
    
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException { //o próprio AuthenticationManager é que verifica a senha e devolve 
//        Usuario appUser = userRepository.findByUsername(username)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
//
//        return new User(
//                appUser.getUsername(),
//                appUser.getPassword(),
//                List.of(new SimpleGrantedAuthority(appUser.getRole().name()))
//        );
//    }
}

/**
 * UserDetailsService
 * 
 * Vai na base e pega o username, no nosso caso o email, o proprio spring security passa isso pra cá
 * 
 * Esse cara é apenas um buscador, ele nao valida nada, nem se o usuario está ativo nem nada.
 * Aqui nao se verifica a senha, quem faz isso é o AutenticationManager
 * 
 * Esse cara pode esta coletando usaurio de qualquer repositorio, pode ser no banco ou em uma lista em uma planilha ou arquivo de texto
 * 
 *  **/

