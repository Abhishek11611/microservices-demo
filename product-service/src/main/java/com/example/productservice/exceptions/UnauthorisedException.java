package com.example.productservice.exceptions;

public class UnauthorisedException extends RuntimeException {
    public UnauthorisedException(String message) {
        super(message);
    }
}
