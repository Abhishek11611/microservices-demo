package com.example.demo.exceptions;

import com.example.demo.dtos.EmptyAPIResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnauthorisedException.class)
    public ResponseEntity<EmptyAPIResponse> handleUnauthorisedException(UnauthorisedException e) {
        System.out.println(e.getMessage());
        String message = e.getMessage();
        EmptyAPIResponse response = new EmptyAPIResponse(message, false);
        return new ResponseEntity<>(response,HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(TokenGenerationException.class)
    public ResponseEntity<EmptyAPIResponse> handleTokenGenerationException(TokenGenerationException e) {
        System.out.println(e.getMessage());
        String message = e.getMessage();
        EmptyAPIResponse response = new EmptyAPIResponse(message, false);
        return new ResponseEntity<>(response,HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<EmptyAPIResponse> handleBadRequestException(BadRequestException e) {
        System.out.println(e.getMessage());
        String message = e.getMessage();
        EmptyAPIResponse response = new EmptyAPIResponse(message, false);
        return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<EmptyAPIResponse> handleNotFoundException(NotFoundException e) {
        System.out.println(e.getMessage());
        String message = e.getMessage();
        EmptyAPIResponse response = new EmptyAPIResponse(message, false);
        return new ResponseEntity<>(response,HttpStatus.NOT_FOUND);
    }
}
