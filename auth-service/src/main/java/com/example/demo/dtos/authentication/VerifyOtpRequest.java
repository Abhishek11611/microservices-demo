package com.example.demo.dtos.authentication;

public record VerifyOtpRequest(String recipient, String otpCode) {
}
