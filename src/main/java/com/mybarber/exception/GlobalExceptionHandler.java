package com.mybarber.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice 
public class GlobalExceptionHandler {
    
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResposta> tratarRecursoNaoEncontrado(RecursoNaoEncontradoException exception){

        ErroResposta erro = new ErroResposta(HttpStatus.NOT_FOUND.value(), exception.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    @ExceptionHandler (MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> tratarErroValidacao(MethodArgumentNotValidException exception){

        String mensagem = exception.getBindingResult().getFieldErrors().get(0).getDefaultMessage();

        ErroResposta erro = new ErroResposta(HttpStatus.BAD_REQUEST.value(), mensagem);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);

    }
}
