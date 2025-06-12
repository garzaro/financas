package com.cleber.financas;

import com.cleber.financas.service.SenhaService;
import com.cleber.financas.service.impl.SenhaServiceImpl;
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
			String hash = senhaService.hashSenha("Cl3b3r@g4rz4r0");
			System.out.println("ESSE É O HASH GERADO: " + hash);
		}

	}
