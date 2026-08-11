package com.example.demo.service.onboard;

import com.example.demo.dtos.registration.PasswordRequest;
import com.example.demo.dtos.registration.RegistrationOTPRequest;
import com.example.demo.dtos.registration.RegistrationPersonalDetailsRequest;
import com.example.demo.dtos.registration.RegistrationResponse;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface UsersRegistrationService {

    RegistrationResponse registrationPersonalDetails(RegistrationPersonalDetailsRequest registrationPersonalDetailsRequest);

    RegistrationResponse registrationVerifyOtp(RegistrationOTPRequest registrationOTPRequest);

    RegistrationResponse registrationPassword(PasswordRequest passwordRequest) throws JsonProcessingException;


}



