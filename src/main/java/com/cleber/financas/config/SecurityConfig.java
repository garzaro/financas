package com.cleber.financas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;

/**
 * TODO-list
 * [] Verificar 401 ao salvar lancamento
 * **/

/** Contexto **/

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	private final JwtFilter jwtFilter; // Seu filtro customizado de JWT

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

//    private UserDetailsService securityUserDetailsService;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
				/**Desabilita CSRF (APIs baseadas em Token) - method reference**/
				.csrf(AbstractHttpConfigurer::disable)

				/**Configura a política de sessão sem estado — nenhuma HttpSession criada. **/
				.sessionManagement(session -> session
						.sessionCreationPolicy(SessionCreationPolicy.STATELESS))			

				/**regras de autorização de rotas - endpoint de login e cadastro **/
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.POST, "/api/join/passwordless-auth/**").permitAll() //permite o inscrito fazer login
						.requestMatchers(HttpMethod.POST, "/api/join/sign-up/**").permitAll() //permite qualquer um cadastrar
						
						/** 👇 LIBERA AS ROTAS DO SWAGGER E OPENAPI 👇**/
	                    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
//	                    .requestMatchers("/actuator/**").hasRole("ADMIN")
//	                    .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
	                    .requestMatchers("/api/**").hasRole("USER")
						
	                    /**qualquer outra requisicao deve estar autenticado**/
						.anyRequest().authenticated()	    			
				)
				
				/** Configurei o provedor de autenticação personalizado. **/
//        		.userDetailsService(securityUserDetailsService)
				
				/**Tratamento de excessao (Mostra 401, não redirecionar para tela de login) - VER JwtAuthenticationEntryPoint**/
				.exceptionHandling(ex -> ex
						.authenticationEntryPoint((request, response, authException) -> {
							response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
							response.setContentType(MediaType.APPLICATION_JSON_VALUE);
							response.getWriter().write("{\"error\": \"Não autorizado Papai\"}");
						}))
				
				/** 
				 * Usuario não envia usuario e senha a cada requisião,
				 *  apenas envia o token no cabeçalho de cada requisição.
				 *  O spring security identifica quem esta fazendo a requisicap
				 *  para decidir se tem ou nao autorizacao
				 *  Por isso a importancia de adicionar o filtro do JWT ANTES do 
				 *  filtro padrão de autenticação por usuário/senha, então:
				 *  
				 *  O filtro pega o token do cabeçalho.
				 *  Valida se o token é legítimo e não expirou.
				 *  Extrai o usuário e suas permissões.
				 *  Autentica o usuário no contexto do Spring - SecurityContextHolder, o cofrinho
				 *  
				 *  **/
				.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class) //UNPAF carrega os dados do usuário e suas Authorities/permissões) e injeta no cofrinho.
				
				/** pra uso do postman/Insomnia **/
	    		.httpBasic(Customizer.withDefaults())			   			 
				
				.build();    	
    }
    
    /**
     * Define o algoritmo de criptografia que o Manager vai usar mano para testar a
     * senha
     **/
    @Bean
    PasswordEncoder passwordEncoder() {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }
    
      	
    	
//    /**injecao manual do passwordEncoder e userDetailsService - Vai aparecer warning na stacktrace**/
//    @Bean
//    AuthenticationProvider authenticationProvider() {
//        /**
//         * Estou falando para o provedor como buscar o usuario (banco de dados)
//         * através da injeção via construtor, pois o construtor vazio e o 
//         * setUserDetailsService foram depreciados.
//         **/
//        DaoAuthenticationProvider authProvider =
//                new DaoAuthenticationProvider(securityUserDetailsService);
//        /**deprecated**/
////        authProvider.setUserDetailsService(securityUserDetailsService);
//
//        /** Estou falando para o provedor como validar a senha criptograda (Argon2)**/
//        authProvider.setPasswordEncoder(passwordEncoder());
//        return authProvider;
//    }
//
//    /** Expõe o gerenciador de autenticação para o Controller **/
//    @Bean
//    AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
//        return authConfig.getAuthenticationManager();
//    }
    
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
 * 
 * Para paginas web, onde que renderiza a pafina é o navegador
 * 
 * .formLogin(configurer -> configurer
 * 	    				.loginPage("/login") 
 *      				.loginProcessingUrl("/login")
 *        				.successForwardUrl("/home")
 *         				.defaultSuccessUrl("/home", true)
 *         				.permitAll())
 *         	    		.logout(logout -> logout
 *         				.logoutUrl("/logout")
 *         				.logoutSuccessUrl("/public")
 *         				.permitAll())
 ***/
