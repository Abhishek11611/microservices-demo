package com.example.demo.controllers.auth;

import com.example.demo.dtos.BaseAPIResponse;
import com.example.demo.dtos.authentication.TokenResponseDTO;
import com.example.demo.service.auth.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/authentication")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/verify-users")
    public ResponseEntity<BaseAPIResponse<TokenResponseDTO>> verifyOtp(@RequestParam String email, HttpServletRequest servletRequest) throws JOSEException {
        TokenResponseDTO tokenResponseDTO = authenticationService.verifyUsers(email, servletRequest);
        BaseAPIResponse<TokenResponseDTO> response = new BaseAPIResponse<>(tokenResponseDTO,"verified successfully",true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


}
