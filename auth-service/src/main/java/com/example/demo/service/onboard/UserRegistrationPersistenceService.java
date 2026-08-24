package com.example.demo.service.onboard;

import com.example.demo.dtos.registration.RegistrationDTO;

public interface UserRegistrationPersistenceService {

    void saveAuthUserWithEvent (RegistrationDTO registrationDTO);

}
