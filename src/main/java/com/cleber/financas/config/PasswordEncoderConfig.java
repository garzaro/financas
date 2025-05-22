package com.cleber.financas.config;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import org.springframework.stereotype.Component;
/*Argon*/
/*classe utilitária para encapsular a lógica de hashing*/
@Component
public class PasswordEncoderConfig {
    private static final Argon2 ARGON_2 = Argon2Factory.create(
            Argon2Factory.Argon2Types.ARGON2i, /*salt*/16, /*hash*/32
    );
    /*numero de iteraçoes*/
    private static final int ITERATIONS = 3;
    /*memoria que sera usada, em KiB - 65 MB*/
    private static final int MEMORY = 65536;
    /*numero de threads*/
    private static final int PARALLELISM = 4;

    public String encode(String senha){
        return ARGON_2.hash(ITERATIONS, MEMORY, PARALLELISM, senha.toCharArray());
    }
    public boolean matches(String senhaDigitada, String hashArmazenado){
        return ARGON_2.verify(hashArmazenado, senhaDigitada.toCharArray());
    }
}
