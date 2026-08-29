package com.example.demo.dtos.events;

import java.time.LocalDate;
import java.util.List;

public record UserRegisteredEvent(String userCode,String firstName,
        String lastName, String email,String mobileNumber,LocalDate dateOfBirth, List<String> role) {

}




