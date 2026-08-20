package com.cleber.financas.exception;

public class ErroDeAutenticacao extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public ErroDeAutenticacao(String messageErroAutenticacao) {
        super(messageErroAutenticacao);
    }
}
