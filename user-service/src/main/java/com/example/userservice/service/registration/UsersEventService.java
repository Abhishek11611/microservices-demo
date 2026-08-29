package com.example.userservice.service.registration;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.kafka.support.Acknowledgment;

public interface UsersEventService {

    void saveUsers(String json, Acknowledgment acknowledgment) throws JsonProcessingException;
}
