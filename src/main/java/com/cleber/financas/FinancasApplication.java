package com.cleber.financas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Todo-list
 * [] retono de badcredencial no frontend - credesnciais invlaidas no toater
 * [] verificar usuario ativo ao ser craido na base
 * [] criar endpoint de login no front, que vai retornar o accesstoken JWT
 * [x] testar no front com usuario e senha qualquer, não existe esse usuario na base
 * [x] ver se a porta do vite é a 3000
 * [] atulizando apagina logada retorna pra pagina de login
 * @EnableConfigurationProperties - se falhar a aplicação nem sobe,
 * melhor maneira de configurar propriedades do projeto

 *
 * **/
@EnableScheduling
@EnableConfigurationProperties
@SpringBootApplication
public class FinancasApplication implements WebMvcConfigurer {

	public static void main(String[] args) {

		SpringApplication.run(FinancasApplication.class, args);
		}
	}


