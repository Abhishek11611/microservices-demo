package com.example.demo.service.onboard;

import com.example.demo.dtos.events.UserRegisteredEvent;
import com.example.demo.entities.users.AuthUser;
import org.springframework.stereotype.Component;

@Component
public class UserRegisteredEventFactory {

    public UserRegisteredEvent create(AuthUser user) {

        return new UserRegisteredEvent(
                user.getUserCode(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getMobileNumber(),
                user.getDateOfBirth()
        );
    }
}
