package com.example.demo.service.security;

import com.example.demo.dtos.authentication.SendOTPDTO;
import com.example.demo.dtos.authentication.TokenResponseDTO;
import com.example.demo.dtos.authentication.VerifyOtpRequest;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthenticationService {

    String sendOtp (SendOTPDTO sendOTPDTO);

    TokenResponseDTO verifyOTP(VerifyOtpRequest verifyOtpRequest, HttpServletRequest servletRequest) throws JOSEException;
    String testAccess();
}
