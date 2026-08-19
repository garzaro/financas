package com.cleber.financas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
//import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
/*Habilitando cors -*/
//@EnableWebMvc

/**
 * implementação (navegador)
 * forma nativa do Spring MVC para configurar CORS globalmente
 * sem Sring Security
 **/
@Configuration
public class CorsConfig implements WebMvcConfigurer {

	@Override
	public void addCorsMappings(@NonNull CorsRegistry registry) {
		registry.addMapping("/api/**")
				.allowedOrigins("http://localhost:3000")
				.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
				.allowedHeaders("*")
				.allowCredentials(true)
				.maxAge(3600);
	}
}

/*
 * para permitir somente requisições vindas de um dominio especifico,
 * e permitir somente metodos GET e POST, o codigo seria assim:
 * public class CorsConfig implements WebMvcConfigurer{
 * 
 * @Override
 * public void addCorsMappings(CorsRegistry registry) {
 * registry.addMapping("/api/**")
 * .allowedOrigins("http://seu-dominio.com")
 * .allowedMethods("GET", "POST");
 * }
 */
