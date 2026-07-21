package com.cleber.financas.api.resource.common;

import com.cleber.financas.api.dto.CampoErro;
import com.cleber.financas.api.dto.ErroResposta;
import com.cleber.financas.exception.ErroDeAutenticacao;
import com.cleber.financas.exception.ErroResponse;
import com.cleber.financas.exception.ErroValidacaoException;
import com.cleber.financas.exception.RegraDeNegocioException;
import com.cleber.financas.exception.EmailJaCadastradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.List;
import java.util.stream.Collectors;

/*para capturar a exceção - la no front ao fazer login com email inexistente*/

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ErroDeAutenticacao.class)
    public final ResponseEntity<ErroResponse> handleErroDeAutenticacao(ErroDeAutenticacao ex, WebRequest request){
        ErroResponse erro = new ErroResponse(ex.getMessage());
        return new ResponseEntity<>(erro, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public final ResponseEntity<ErroResponse> handleBadCredentialsException(BadCredentialsException ex) {
        ErroResponse erro = new ErroResponse("credenciais inválidas");
        return new ResponseEntity<>(erro, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(EmailJaCadastradoException.class)
    public final ResponseEntity<ErroResposta<CampoErro>> handleEmailJaCadastradoException(EmailJaCadastradoException ex) {
        ErroResposta<CampoErro> erro = ErroResposta.conflitoPadrao(ex.getMessage());
        return new ResponseEntity<>(erro, HttpStatus.CONFLICT);
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

    /**
     * Trata erros de validação de argumentos de método (@Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta<CampoErro>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<CampoErro> erros = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> new CampoErro(error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());

        ErroResposta<CampoErro> erro = new ErroResposta<>("Erro de validação", HttpStatus.BAD_REQUEST.value(), erros);
        return new ResponseEntity<>(erro, HttpStatus.BAD_REQUEST);
    }
}
