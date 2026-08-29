package com.example.demo.service.onboard;

import com.example.demo.dtos.events.UserRegisteredEvent;
import com.example.demo.entities.users.AuthUser;
import com.example.demo.entities.users.Role;
import com.example.demo.entities.users.UserRoles;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserRegisteredEventFactory {

    public UserRegisteredEvent create(AuthUser user) {

        List<String> roles = user.getUserRoles()
                .stream().map(u -> u.getRole().getName())
                .toList();


        return new UserRegisteredEvent(
                user.getUserCode(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getMobileNumber(),
                user.getDateOfBirth(),
                roles
        );
    }
}
