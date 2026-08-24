package com.example.demo.dtos.events;

import java.time.LocalDate;

public record UserRegisteredEvent(String userCode,String firstName,
        String lastName, String email,String mobileNumber,LocalDate dateOfBirth) {

}




