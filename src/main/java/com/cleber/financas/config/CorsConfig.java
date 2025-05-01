package com.cleber.financas.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableWebMvc
@Configuration
public class CorsConfig  implements WebMvcConfigurer {
	/*Habilitando cors -*/
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/api/")
				.allowedOrigins("http://localhost:3000")
				.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
				.allowedHeaders("*")
				//.allowedHeaders("Content-Type", "application/json", "Authentication")
				.allowCredentials(false)
				.maxAge(-1);
	}
}

/*@EnableWebMvc
@Configuration
public class CorsConfig  implements WebMvcConfigurer{
	/*Habilitando cors -
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/api/")
				.allowedOrigins("http://localhost:3000")
				.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
				.allowedHeaders("*")
				//.allowedHeaders("Content-Type", "application/json", "Authentication")
				.allowCredentials(false)
				.maxAge(-1);
	}
*/

/*para permitir somente requisições vindas de um dominio especifico,
e permitir somente metodos GET e POST, o codigo seria assim:

@Override
public void addCorsMappings(CorsRegistry registry) {
	registry.addMapping("/api/**")
			.allowedOrigins("http://seu-dominio.com")
			.allowedMethods("GET", "POST");
}
*/


