package com.cleber.financas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.*;

/*@EnableWebMvc - para liberar o acesso - configuração de CORS
* allowedOrigins - (url) - urls permitidas para acessar um site
* CorsRegistry registry - permite adicionar configurações de CORS*/

@SpringBootApplication
@EnableWebMvc
public class FinancasApplication implements WebMvcConfigurer {
	/*Habilitando de cors -*/
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		WebMvcConfigurer.super.addCorsMappings(registry);
		registry.addMapping("/**").allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
	}
	
	public static void main(String[] args) {

		SpringApplication.run(FinancasApplication.class, args);
	}
}

/*para permitir somente requisições vindas de um dominio especifico,
e permitir somente metodos GET e POST, o codigo seria assim:

@Override
public void addCorsMappings(CorsRegistry registry) {
	registry.addMapping("/api/**")
			.allowedOrigins("http://seu-dominio.com")
			.allowedMethods("GET", "POST");
}
*/
