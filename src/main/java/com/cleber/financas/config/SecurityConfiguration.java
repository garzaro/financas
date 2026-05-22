package com.cleber.financas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**Contexto**/

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
	
	/**Expõe o AuthenticationManager para ser injetado no Controller**/
	
	
	@Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{    	
    	
        http
                       
        .csrf(AbstractHttpConfigurer::disable)
              
        /**Sessão sem estado — nenhuma HttpSession criada.**/  
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        
        /**regras de autorização de rotas**/
        .authorizeHttpRequests(auth -> auth
        		.requestMatchers(HttpMethod.POST, "/api/usuario/auth").permitAll() 
        		.requestMatchers(HttpMethod.POST, "/api/usuario").permitAll() //permitindo qualquer um cadastrar
                .requestMatchers("/actuator/**").hasRole("ADMIN")              
                .anyRequest().authenticated() // qualqer outra requisicao deve esta autenticado       		
        		)
               	
        
        //.requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
//            .headers(headers -> headers
//               .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
//               .contentSecurityPolicy(csp -> csp
//               .policyDirectives("default-src 'self'")
//               )
//            )
//                .securityMatcher("/login")
//                .csrf(csrf -> csrf
//                    .ignoringRequestMatchers("/**")
//                ) /*stateless*/
//                .authorizeHttpRequests(autorizacao -> {
//                    autorizacao
//                            .requestMatchers(acessoPublico).permitAll()
//                            .requestMatchers("/home").permitAll() /*precisa definir role ("USER")*/
//                            .anyRequest().authenticated();
//                })
                .formLogin(formulario -> formulario
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/public") /*definir redirecionamento*/
                        .permitAll()
                )
                .httpBasic(Customizer.withDefaults()
                		);
        return http.build();
    }
	
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) 
			throws Exception{
		return config.getAuthenticationManager();
	}	
}

/*
.csrf(AbstractHttpConfigurer::disable)
httpSecurity - objeto reside dentro do contexto de seguranca do spring - pré configurado usado configurar a seguranca
Antes de chamar o build existe varias confs que podem ser feitas - O sfltrc declarado aqui sobrepoe
o padrao  - Exemplo: aquele que habilitou o formulario de login no browser e protegeu nossa api.

Para o formulario padrao do spring security com o .formLogin(Customizer.withDefaults())
Para receber autenticação via formulario de login ou postman .httpBasic(Customizer.withDefaults())

Em <autorizacao> qualquer requisicao feita pra essa api tem que estar autenticado.

{
configurer ->
                        configurer
                                .loginPage("/login")
                                .successForwardUrl("/home"))
}

*/
