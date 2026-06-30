package com.example.demo.service.auth;

import com.example.demo.dtos.authentication.TokenResponseDTO;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthenticationService {

    TokenResponseDTO verifyUsers(String email, HttpServletRequest servletRequest) throws JOSEException;
}
