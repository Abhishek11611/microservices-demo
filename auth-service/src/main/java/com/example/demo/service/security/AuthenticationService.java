package com.example.demo.service.security;

import com.example.demo.dtos.authentication.SendOTPDTO;
import com.example.demo.dtos.authentication.SendOTPResponseDTO;
import com.example.demo.dtos.authentication.TokenResponseDTO;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthenticationService {

    SendOTPResponseDTO sendOtp (SendOTPDTO sendOTPDTO);

    TokenResponseDTO verifyUsers(String email, HttpServletRequest servletRequest) throws JOSEException;
    String testAccess();
}
