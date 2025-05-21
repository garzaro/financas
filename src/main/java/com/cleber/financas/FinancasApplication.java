package com.cleber.financas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.*;

/*@EnableWebMvc - para liberar o acesso - configuração de CORS
* allowedOrigins - (url) - urls permitidas para acessar um site
* CorsRegistry registry - permite adicionar configurações de CORS*/
@SpringBootApplication
public class FinancasApplication implements WebMvcConfigurer {

	public static void main(String[] args) {

		SpringApplication.run(FinancasApplication.class, args);
	}
}