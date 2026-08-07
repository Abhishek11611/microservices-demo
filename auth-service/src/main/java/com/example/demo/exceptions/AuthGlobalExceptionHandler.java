package com.example.demo.exceptions;


import com.example.commoncore.dto.EmptyAPIResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthGlobalExceptionHandler {

    @ExceptionHandler(TokenGenerationException.class)
    public ResponseEntity<EmptyAPIResponse> handleTokenGenerationException(TokenGenerationException e) {
        System.out.println(e.getMessage());
        String message = e.getMessage();
        EmptyAPIResponse response = new EmptyAPIResponse(message, false);
        return new ResponseEntity<>(response,HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
