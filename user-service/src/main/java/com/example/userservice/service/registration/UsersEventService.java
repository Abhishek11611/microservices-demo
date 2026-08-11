package com.example.userservice.service.registration;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface UsersEventService {

    void saveUsers(String json) throws JsonProcessingException;
}
