package com.cleber.financas.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.cleber.financas.security.JwtAuthenticationFilter;

/**
 * TODO-list
 * [] Verificar 401 ao salvar lancamento
 * **/

/** Contexto **/

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final UsuarioDetailsService usuarioDetailsService;

	public SecurityConfig(
			JwtAuthenticationFilter jwtAuthenticationFilter,
			UsuarioDetailsService usuarioDetailsService
			) {
			super();
			this.usuarioDetailsService = usuarioDetailsService;
			this.jwtAuthenticationFilter = jwtAuthenticationFilter;
	}   
	
 @Bean
    public PasswordEncoder passwordEncoder() {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
				/**Desabilita CSRF (APIs baseadas em Token) - method reference**/
				.csrf(AbstractHttpConfigurer::disable)
				/** Habilita as configurações de CORS **/
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))

				/**Configura a política de sessão sem estado — nenhuma HttpSession criada. **/
				.sessionManagement(session -> session
						.sessionCreationPolicy(SessionCreationPolicy.STATELESS))			

				/**regras de autorização de rotas - endpoint de login e cadastro **/
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/register", "/api/auth/join/sign-up/**").permitAll() // caminho real do controlador
						/** 👇 LIBERA AS ROTAS DO SWAGGER E OPENAPI 👇**/
	                    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
//	                    .requestMatchers("/actuator/**").hasRole("ADMIN")
//	                    .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
	                    .requestMatchers("/api/**").hasRole("ROLE_USER")
	                    /**qualquer outra requisicao deve estar autenticado**/
						.anyRequest().authenticated()	    			
				)
				.authenticationProvider(authenticationProvider())

				
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
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class) //UNPAF carrega os dados do usuário e suas Authorities/permissões) e injeta no cofrinho.
				
				/** pra uso do postman/Insomnia **/
//	    		.httpBasic(Customizer.withDefaults())
				
				.build();
		
	}
					@Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(usuarioDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /** Expõe o gerenciador de autenticação para o Controller **/
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000")); // ajuste para o domínio real do front
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
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
 ***/
