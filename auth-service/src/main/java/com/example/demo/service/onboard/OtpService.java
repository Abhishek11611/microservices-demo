package com.example.demo.service.onboard;

public interface OtpService {

    String generateAndStore(String journeyId);

    Boolean verifyOtp(String journeyId, String requestOTP);
}
