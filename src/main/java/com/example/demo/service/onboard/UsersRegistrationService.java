package com.example.demo.service.onboard;

import com.example.demo.dtos.registration.RegistrationPersonalDetailsRequest;
import com.example.demo.dtos.registration.RegistrationResponse;

public interface UsersRegistrationService {

    RegistrationResponse registrationPersonalDetails(RegistrationPersonalDetailsRequest registrationPersonalDetailsRequest);

    RegistrationResponse registrationVerifyOtp(RegistrationPersonalDetailsRequest registrationPersonalDetailsRequest);


}
