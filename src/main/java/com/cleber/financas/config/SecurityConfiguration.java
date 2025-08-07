package com.cleber.financas.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        String[] acessoPublico = { "/", "/register", "/login", "/logout" };
        http
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                        .contentSecurityPolicy(csp -> csp
                        .policyDirectives("default-src 'self'")
                        )
                )
                .securityMatcher("/login")
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**")
                        ) /*stateless*/
                .authorizeHttpRequests(autorizacao -> {
                    autorizacao
                            .requestMatchers(acessoPublico).permitAll()
                            .requestMatchers("/home").permitAll() /*precisa definir role ("USER")*/
                            .anyRequest().authenticated();
                })
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
