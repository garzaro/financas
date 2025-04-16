package com.cleber.financas;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableWebMvc
@Configuration
public class CorsConfig  implements WebMvcConfigurer{
	/*Habilitando de cors -*/
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
				.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
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

}
