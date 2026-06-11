package com.cleber.financas.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import lombok.RequiredArgsConstructor;

/** Contexto **/

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Autowired
    private UserDetailsService securityUserDetailsService;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(Customizer.withDefaults()) // Adiciona o filtro de CORS no Spring Security, respeita e obedece as regras da classe CorsConfig 
                .csrf(AbstractHttpConfigurer::disable) // usando method reference aqui só pra xiarrrr
                /** Sessão sem estado — nenhuma HttpSession criada. **/
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                /** regras de autorização de rotas **/
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/usuarios/autenticar").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/usuarios").permitAll() // permitindo qualquer um
                                                                                      // cadastrar
                        // 👇 LIBERA AS ROTAS DO SWAGGER E OPENAPI 👇
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html")
                        .permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
                        .anyRequest().authenticated() // qualqer outra requisicao deve esta autenticado
                )

                /** Configurei o provedor de autenticação personalizado. **/
                // .userDetailsService(securityUserDetailsService)

                .formLogin(formulario -> formulario
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/public") /* definir redirecionamento */
                        .permitAll())
                /** pra uso do postman/Insomnia **/
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }
    /**injecao manual do passwordEncoder e userDetailsService - Vai aparecer warning na stacktrace**/
    @Bean
    AuthenticationProvider authenticationProvider() {
        /**
         * Estou falando para o provedor como buscar o usuario (banco de dados)
         * através da injeção via construtor, pois o construtor vazio e o 
         * setUserDetailsService foram depreciados.
         **/
        DaoAuthenticationProvider authProvider =
                new DaoAuthenticationProvider(securityUserDetailsService);
        /**deprecated**/
//        authProvider.setUserDetailsService(securityUserDetailsService);

        /** Estou falando para o provedor como validar a senha criptograda (Argon2)**/
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /** Expõe o gerenciador de autenticação para o Controller **/
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Define o algoritmo de criptografia que o Manager vai usar mano para testar a
     * senha
     **/
    @Bean
    PasswordEncoder passwordEncoder() {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }
}

/**
 * 
 * Para entender como a segurança do Spring funciona na prática, imagine um
 * banco físico.
 * 
 * O SecurityConfig é a planta do prédio e o manual de regras (diz quais portas
 * estão trancadas
 * e quem gerencia a segurança).
 * 
 * O AuthenticationManager é o chefe da segurança na recepção. Ele não conhece
 * os clientes
 * pessoalmente, mas sabe como validar se uma credencial é verdadeira.
 * 
 * O UserDetailsService é o arquivo central do banco. Quando o chefe da
 * segurança precisa verificar um cliente, ele liga para esse setor para buscar
 * a ficha cadastral do usuário no banco de dados.
 * 
 * ***************
 * 
 * .csrf(csrf -> csrf.disable())
 * 
 * httpSecurity - objeto reside dentro do contexto de seguranca do spring -
 * pré configurado usado para configurar a seguranca antes de chamar o build.
 * 
 * existe varias confs que podem ser feitas -
 * 
 * O sfltrc declarado aqui sobrepoe o padrao -
 * Exemplo: aquele que habilitou o formulario de login no browser e protegeu
 * nossa api.
 * 
 * Para o formulario padrao do spring security com o
 * .formLogin(Customizer.withDefaults())
 * 
 * Para receber autenticação via formulario de login ou postman
 * .httpBasic(Customizer.withDefaults())
 * 
 * 
 * { configurer -> configurer
 * .loginPage("/login")
 * .successForwardUrl("/home"))
 * }
 * *
 **/
