package com.cleber.financas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/*@EnableWebMvc - para liberar o acesso  -CORS
* allowedOrigins - (url) - urls permitidas para acessar um site */
@SpringBootApplication
@EnableWebMvc
public class FinancasApplication implements WebMvcConfigurer {
	/*Adicionando mapeamento de cors*/
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		WebMvcConfigurer.super.addCorsMappings(registry);
		registry.addMapping("/**").allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
	}
	
	public static void main(String[] args) {

		SpringApplication.run(FinancasApplication.class, args);
	}

}
