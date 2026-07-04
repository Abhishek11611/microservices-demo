package com.example.demo.service.security;

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
//        String roles = users.getUserRoles().stream()
//                .map(ur -> ur.getRole().getName())
//                .collect(Collectors.joining(","));
        String accessToken = jwtService.generateAccessToken(email,"ADMIN");
        return new TokenResponseDTO(null,accessToken,null,null);
    }

    @Override
    public String testAccess(){
        return "Hi Admin";
    }
}
