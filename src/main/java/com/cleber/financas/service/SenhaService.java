package com.cleber.financas.service;

import org.springframework.stereotype.Service;

/*definição do contrato*/
@Service
public interface SenhaService {
    String hashSenha(String senha);
    boolean verificarSenha(String senhaDigitada, String hashArmazenado);
}
