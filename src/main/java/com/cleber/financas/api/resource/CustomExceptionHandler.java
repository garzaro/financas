package com.cleber.financas.api.resource;


import com.cleber.financas.exception.ErroDeAutenticacao;
import com.cleber.financas.exception.ErroResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/*para capturar a exceção - la no front ao fazer login com email inexistente*/

@ControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(ErroDeAutenticacao.class)
    public final ResponseEntity<ErroResponse> handleErroDeAutenticacao(ErroDeAutenticacao ex, WebRequest request){
        ErroResponse erro = new ErroResponse(ex.getMessage());
        return new ResponseEntity<>(erro, HttpStatus.UNAUTHORIZED);
    }
}
