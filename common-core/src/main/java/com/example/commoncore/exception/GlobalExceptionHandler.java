package com.example.commoncore.exception;

import com.example.commoncore.dto.EmptyAPIResponse;
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
        e.printStackTrace();
        return new ResponseEntity<>(response,HttpStatus.UNAUTHORIZED);
    }


    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<EmptyAPIResponse> handleBadRequestException(BadRequestException e) {
        System.out.println(e.getMessage());
        String message = e.getMessage();
        EmptyAPIResponse response = new EmptyAPIResponse(message, false);
        e.printStackTrace();
        return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<EmptyAPIResponse> handleNotFoundException(NotFoundException e) {
        System.out.println(e.getMessage());
        String message = e.getMessage();
        EmptyAPIResponse response = new EmptyAPIResponse(message, false);

        e.printStackTrace();
        return new ResponseEntity<>(response,HttpStatus.NOT_FOUND);
    }
}
