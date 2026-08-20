package com.cleber.financas.exception;

public class ErroAcessoBancoDadosException extends RuntimeException{
    private static final long serialVersionUID = 1L;
    public ErroAcessoBancoDadosException(String erroBd) {
        super(erroBd);
    }
}
