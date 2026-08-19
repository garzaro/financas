package com.cleber.financas.service.impl;

import com.cleber.financas.service.SenhaService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
/*logica de hashing encapsulada*/
@Service
public class SenhaServiceImpl implements SenhaService {

    private final PasswordEncoder passwordEncoder;

    public SenhaServiceImpl(PasswordEncoder passwordEncoder) {
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
