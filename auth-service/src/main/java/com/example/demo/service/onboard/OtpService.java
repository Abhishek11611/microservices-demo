package com.example.demo.service.onboard;

public interface OtpService {

    void sendOTP(String identifier);

    Boolean verifyOtp(String identifier, String requestOTP);
}
