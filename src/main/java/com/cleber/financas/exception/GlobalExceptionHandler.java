package com.cleber.financas.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ValorInvalidoException.class)
    public ResponseEntity<?> handleInvalidValueException(ValorInvalidoException e, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        errors.put("Atenção", e.getMessage());
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ErroDataException.class)
    public ResponseEntity<Map<String,String>> handleInvalidDateException(ErroDataException e, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        errors.put("Atenção", e.getMessage());
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleInvalidInput(HttpMessageNotReadableException e) {
        return new ResponseEntity<>("Erro ao ler a data. Verifique o formato dos dados.", HttpStatus.BAD_REQUEST);
    }
}