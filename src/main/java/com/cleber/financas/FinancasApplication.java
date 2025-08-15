package com.cleber.financas;

import com.cleber.financas.service.SenhaService;
import com.cleber.financas.service.impl.SenhaServiceImpl;
import com.password4j.Hash;
import com.password4j.Password;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.web.servlet.config.annotation.*;

/*@EnableWebMvc - para liberar o acesso - configuração de CORS
* allowedOrigins - (url) - urls permitidas para acessar um site
* CorsRegistry registry - permite adicionar configurações de CORS*/
@SpringBootApplication
public class FinancasApplication implements WebMvcConfigurer {

	public static void main(String[] args) {

		SpringApplication.run(FinancasApplication.class, args);

		SenhaService senhaService = new SenhaServiceImpl(new Argon2PasswordEncoder(16,
				32,
				2,
				65536,
				5));
			String hash = senhaService.hashSenha("senha123");
			System.out.println("ESSE É O HASH GERADO (classe de inicialização): " + hash);
		}
	}
