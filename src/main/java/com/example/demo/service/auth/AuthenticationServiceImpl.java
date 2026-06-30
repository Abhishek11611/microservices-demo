package com.example.demo.service.auth;

import com.example.demo.dtos.authentication.TokenResponseDTO;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final JwtService jwtService;

    public AuthenticationServiceImpl(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public TokenResponseDTO verifyUsers(String email, HttpServletRequest servletRequest) throws JOSEException {
        String accessToken = jwtService.generateAccessToken(email);
        return new TokenResponseDTO(null,accessToken,null,null);
    }
}
