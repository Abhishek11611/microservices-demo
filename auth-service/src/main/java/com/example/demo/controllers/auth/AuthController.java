package com.example.demo.controllers.auth;

import com.example.commoncore.dto.BaseAPIResponse;
import com.example.demo.dtos.authentication.SendOTPDTO;
import com.example.demo.dtos.authentication.TokenResponseDTO;
import com.example.demo.dtos.authentication.VerifyOtpRequest;
import com.example.demo.dtos.authentication.VerifyPasswordRequest;
import com.example.demo.service.security.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/send-otp")
    public ResponseEntity<BaseAPIResponse<String>> sendOtp(@RequestBody SendOTPDTO sendOTPDTO, HttpServletRequest servletRequest) throws JOSEException {
        String sendOtpResp = authenticationService.sendOtp(sendOTPDTO);
        BaseAPIResponse<String> response = new BaseAPIResponse<>(sendOtpResp,"Send OTP successfully",true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<BaseAPIResponse<TokenResponseDTO>> verifyOtp(@RequestBody VerifyOtpRequest verifyOtpRequest, HttpServletRequest servletRequest) throws JOSEException {
        TokenResponseDTO tokenResponseDTO = authenticationService.verifyOTP(verifyOtpRequest, servletRequest);
        BaseAPIResponse<TokenResponseDTO> response = new BaseAPIResponse<>(tokenResponseDTO,"verify OTP successfully",true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/refresh-token")
    public ResponseEntity<BaseAPIResponse<TokenResponseDTO>> refreshToken( HttpServletRequest servletRequest) throws JOSEException {
        TokenResponseDTO tokenResponseDTO = authenticationService.refreshToken(servletRequest);
        BaseAPIResponse<TokenResponseDTO> response = new BaseAPIResponse<>(tokenResponseDTO,"Token refreshed successfully",true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/verify-password")
    public ResponseEntity<BaseAPIResponse<TokenResponseDTO>> verifyPassword(@RequestBody VerifyPasswordRequest verifyPasswordRequest, HttpServletRequest servletRequest) throws JOSEException {
        TokenResponseDTO tokenResponseDTO = authenticationService.verifyPassword(verifyPasswordRequest,servletRequest);
        BaseAPIResponse<TokenResponseDTO> response = new BaseAPIResponse<>(tokenResponseDTO,"Password Verified successfully",true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
