package com.cleber.financas.exception;

public class ErroDeValidacao extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public ErroDeValidacao(String messageErroValidacao) {
        super(messageErroValidacao);
    }
}
