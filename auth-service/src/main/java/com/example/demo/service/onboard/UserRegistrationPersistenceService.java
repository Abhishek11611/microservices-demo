package com.example.demo.service.onboard;

import com.example.demo.dtos.registration.RegistrationDTO;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface UserRegistrationPersistenceService {

     void register(RegistrationDTO dto);
}
