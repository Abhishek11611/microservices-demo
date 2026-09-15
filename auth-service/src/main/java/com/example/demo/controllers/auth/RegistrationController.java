package com.example.demo.controllers.auth;

import com.example.commoncore.dto.BaseAPIResponse;
import com.example.demo.dtos.authentication.TokenResponseDTO;
import com.example.demo.dtos.authentication.VerifyOtpRequest;
import com.example.demo.dtos.registration.PasswordRequest;
import com.example.demo.dtos.registration.RegistrationOTPRequest;
import com.example.demo.dtos.registration.RegistrationPersonalDetailsRequest;
import com.example.demo.dtos.registration.RegistrationResponse;
import com.example.demo.service.onboard.UsersRegistrationService;
import com.example.demo.service.security.AuthenticationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/registrations")
public class RegistrationController {

    private final UsersRegistrationService usersRegistrationService;

    public RegistrationController(UsersRegistrationService usersRegistrationService) {
        this.usersRegistrationService = usersRegistrationService;
    }

    @PostMapping("/personal-details")
    public ResponseEntity<BaseAPIResponse<RegistrationResponse>> registrationPersonalDetails(@RequestBody RegistrationPersonalDetailsRequest registrationPersonalDetailsRequest) {
        RegistrationResponse registrationResponse = usersRegistrationService.registrationPersonalDetails(registrationPersonalDetailsRequest);
        BaseAPIResponse<RegistrationResponse> response = new BaseAPIResponse<>(registrationResponse,"Personal Information successfully",true);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<BaseAPIResponse<RegistrationResponse>> registrationVerifyOTP(@RequestBody RegistrationOTPRequest registrationOTPRequest) {
        RegistrationResponse registrationResponse = usersRegistrationService.registrationVerifyOtp(registrationOTPRequest);
        BaseAPIResponse<RegistrationResponse> response = new BaseAPIResponse<>(registrationResponse,"verify OTP successfully",true);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/password")
    public ResponseEntity<BaseAPIResponse<RegistrationResponse>> registrationPassword(@RequestBody PasswordRequest passwordRequest) throws JsonProcessingException {
        RegistrationResponse registrationResponse = usersRegistrationService.registrationPassword(passwordRequest);
        BaseAPIResponse<RegistrationResponse> response = new BaseAPIResponse<>(registrationResponse,"Password Created successfully",true);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


}
