package com.cleber.financas.service.impl;

import com.cleber.financas.service.SenhaService;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;
/*logica de hashing encapsulada*/
@Service
public class SenhaServiceImpl implements SenhaService {

    private final Argon2PasswordEncoder passwordEncoder;

    public SenhaServiceImpl(Argon2PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String hashSenha(String senha) {
        return passwordEncoder.encode(senha);
    }

    @Override
    public boolean verificarSenha(String senhaDigitada, String hashArmazenado) {
        return passwordEncoder.matches(senhaDigitada, hashArmazenado);
    }
}
