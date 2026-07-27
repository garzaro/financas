package com.cleber.financas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Todo-list
 * [] retono de badcredencial no frontend - credesnciais invlaidas no toater
 * [] verificar usuario ativo ao ser craido na base
 * [] criar endpoint de login no front, que vai retornar o token JWT
 * [x] testar no front com usuario e senha qualquer, não existe esse usuario na base
 * [x] ver se a porta do vite é a 3000
 * [] atulizando apagina logada retorna pra pagina de login
 
 * 
 * **/

@SpringBootApplication
public class FinancasApplication implements WebMvcConfigurer {

	public static void main(String[] args) {

		SpringApplication.run(FinancasApplication.class, args);
		}
	}


