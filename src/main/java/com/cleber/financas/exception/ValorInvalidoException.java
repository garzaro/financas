package com.cleber.financas.exception;

public class ValorInvalidoException extends RuntimeException{

    public ValorInvalidoException(String valorInvalido){
        super(valorInvalido);
    }
}
