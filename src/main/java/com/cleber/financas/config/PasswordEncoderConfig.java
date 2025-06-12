package com.cleber.financas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Component;
/*Argon*/
/*classe utilitária para encapsular a lógica de hashing*/
@Component
public class PasswordEncoderConfig {
    @Bean
    public Argon2PasswordEncoder passwordEncoder(){
        return new Argon2PasswordEncoder(
                16,
                32,
                2,
                65536,
                5
        );
    }
	
}

/*
* #Configuração do argon2
16,  // saltLength - Tamanho do salt (padrão recomendado: 16 bytes)
32,  // hashLength - Tamanho do hash gerado (padrão recomendado: 32 bytes)
2,   // parallelism - Número de threads paralelas para computação (2 é um bom equilíbrio)
65536, // memory - Memória usada em KB (64MB - recomendado para segurança)
5    // iterations - Número de iterações, repetições do algoritmo (5 é um valor seguro)
* */
    