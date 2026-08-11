package com.example.demo.controllers.auth;

import com.example.commoncore.dto.BaseAPIResponse;
import com.example.demo.dtos.authentication.TokenResponseDTO;
import com.example.demo.dtos.registration.PasswordRequest;
import com.example.demo.dtos.registration.RegistrationOTPRequest;
import com.example.demo.dtos.registration.RegistrationPersonalDetailsRequest;
import com.example.demo.dtos.registration.RegistrationResponse;
import com.example.demo.service.onboard.UsersRegistrationService;
import com.example.demo.service.onboard.UsersRegistrationServiceImpl;
import com.example.demo.service.security.AuthenticationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/registrations")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final UsersRegistrationService usersRegistrationService;

    public AuthenticationController(AuthenticationService authenticationService, UsersRegistrationService usersRegistrationService) {
        this.authenticationService = authenticationService;
        this.usersRegistrationService = usersRegistrationService;
    }

    @PostMapping("/verify-users")
    public ResponseEntity<BaseAPIResponse<TokenResponseDTO>> verifyOtp(@RequestParam String email, HttpServletRequest servletRequest) throws JOSEException {
        TokenResponseDTO tokenResponseDTO = authenticationService.verifyUsers(email, servletRequest);
        BaseAPIResponse<TokenResponseDTO> response = new BaseAPIResponse<>(tokenResponseDTO,"verified successfully",true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/personal-details")
    public ResponseEntity<BaseAPIResponse<RegistrationResponse>> registrationPersonalDetails(RegistrationPersonalDetailsRequest registrationPersonalDetailsRequest) {
        RegistrationResponse registrationResponse = usersRegistrationService.registrationPersonalDetails(registrationPersonalDetailsRequest);
        BaseAPIResponse<RegistrationResponse> response = new BaseAPIResponse<>(registrationResponse,"Personal Information successfully",true);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<BaseAPIResponse<RegistrationResponse>> registrationVerifyOTP(RegistrationOTPRequest registrationOTPRequest) {
        RegistrationResponse registrationResponse = usersRegistrationService.registrationVerifyOtp(registrationOTPRequest);
        BaseAPIResponse<RegistrationResponse> response = new BaseAPIResponse<>(registrationResponse,"verify OTP successfully",true);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/password")
    public ResponseEntity<BaseAPIResponse<RegistrationResponse>> registrationPassword(PasswordRequest passwordRequest) throws JsonProcessingException {
        RegistrationResponse registrationResponse = usersRegistrationService.registrationPassword(passwordRequest);
        BaseAPIResponse<RegistrationResponse> response = new BaseAPIResponse<>(registrationResponse,"Password Created successfully",true);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


}
