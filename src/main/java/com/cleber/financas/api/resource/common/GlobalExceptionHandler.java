package com.cleber.financas.api.resource.common;

import com.cleber.financas.api.dto.ErroResposta;
import com.cleber.financas.exception.ErroDeAutenticacao;
import com.cleber.financas.exception.ErroResponse;
import com.cleber.financas.exception.ErroValidacaoException;
import com.cleber.financas.exception.RegraDeNegocioException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

/*para capturar a exceção - la no front ao fazer login com email inexistente*/

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ErroDeAutenticacao.class)
    public final ResponseEntity<ErroResponse> handleErroDeAutenticacao(ErroDeAutenticacao ex, WebRequest request){
        ErroResponse erro = new ErroResponse(ex.getMessage());
        return new ResponseEntity<>(erro, HttpStatus.UNAUTHORIZED);
    }
    
    /**
     * Trata erros de validação dos campos
     */
    @ExceptionHandler(ErroValidacaoException.class)
    public final ResponseEntity<ErroResposta> handleErroValidacao(ErroValidacaoException ex, WebRequest request) {
        ErroResposta erro = ErroResposta.respostaPadrao(ex.getMessage());
        return new ResponseEntity<>(erro, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Trata erros de regra de negócio
     */
    @ExceptionHandler(RegraDeNegocioException.class)
    public final ResponseEntity<ErroResposta> handleRegraDeNegocio(RegraDeNegocioException ex, WebRequest request) {
        ErroResposta erro = ErroResposta.conflitoPadrao(ex.getMessage());
        return new ResponseEntity<>(erro, HttpStatus.CONFLICT);
    }
}
