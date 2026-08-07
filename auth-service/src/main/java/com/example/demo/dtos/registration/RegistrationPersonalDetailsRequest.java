package com.example.demo.dtos.registration;

import java.time.LocalDate;

public record RegistrationPersonalDetailsRequest(String firstName,
                                                 String lastName,
                                                 String email,
                                                 String mobileNumber,
                                                 LocalDate dateOfBirth) { }
