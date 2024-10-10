package com.cleber.financas.api.resource;

import com.cleber.financas.exception.ErroDataException;
import com.cleber.financas.exception.ValorInvalidoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.DateTimeException;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ValorInvalidoException.class)
    public ResponseEntity<?> handleInvalidValueException(ValorInvalidoException e) {
        Map<String, String> errors = new HashMap<>();
        errors.put("Atenção", e.getMessage());
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
    /*NAO ESTA FUNICIOINADO*/
    @ExceptionHandler(ErroDataException.class)
    public DateTimeException handleErroDataException(ErroDataException e) {
        Map<String, String> errors = new HashMap<>();
        errors.put("Atenção", e.getMessage());
        return new DateTimeException("daINAVLIDA TIOZAO");
    }
}