package com.cleber.financas.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.cleber.financas.security.JwtAuthenticationFilter;

/** Contexto **/

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final UsuarioDetailsService usuarioDetailsService;

    @Value("${app.cors.allowed-origins}")
    private List<String> allowedOrigins;

	public SecurityConfig(
			JwtAuthenticationFilter jwtAuthenticationFilter,
			UsuarioDetailsService usuarioDetailsService
			) {
			super();
			this.jwtAuthenticationFilter = jwtAuthenticationFilter;
			this.usuarioDetailsService = usuarioDetailsService;
	}

    @Bean
    public PasswordEncoder passwordEncoder() {
        /**Argon2id nativo via Bouncy Castle**/
        int saltLength = 16;         // 16 bytes (128 bits)
        int hashLength = 32;         // 32 bytes (256 bits)
        int parallelism = 1;         // 1 thread
        int memory = 1 << 16;        // 64 MiB (65536 KiB)
        int iterations = 3;          // 3 iterações

        return new Argon2PasswordEncoder(
                saltLength,
                hashLength,
                parallelism,
                memory,
                iterations
        );
    }
    // @Bean
    // public PasswordEncoder passwordEncoder() {
    //     return new Argon2PasswordEncoder(16, 32, 1, 1 << 16, 3);
    // }

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
                .authenticationProvider(authenticationProvider())
                /**regras de autorização de rotas - endpoint de login e cadastro **/
				.authorizeHttpRequests(auth -> auth
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.requestMatchers(
                            "/api/auth/sign-in",
                            "/api/auth/join/sign-up",
                            "/api/auth/refresh",
                            "/v3/api-docs/**",
                            "/swagger-ui/**",
                            "/swagger-ui.html"
                        ).permitAll()
                    /**qualquer outra requisicao deve estar autenticado**/
					.anyRequest().authenticated()
				)
				
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class) //UNPAF carrega os dados do usuário e suas Authorities/permissões) e injeta no cofrinho.
				/** pra uso do postman/Insomnia **/
//	    		.httpBasic(Customizer.withDefaults())
				.build();

	}
	@Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider =
        		new DaoAuthenticationProvider(usuarioDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /** Expõe o gerenciador de autenticação para o Controller **/
    @Bean
    AuthenticationManager authenticationManager(
    		AuthenticationConfiguration authConfig)
    				throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins((allowedOrigins));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(List.of(
            "Authorization",
            "Cache-Control",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

