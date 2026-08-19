package com.cleber.financas.config;

import com.cleber.financas.security.crypto.Argon2JvmPasswordEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new Argon2JvmPasswordEncoder();
    }
}
