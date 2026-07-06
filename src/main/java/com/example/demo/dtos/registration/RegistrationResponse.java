package com.example.demo.dtos.registration;

import com.example.demo.enums.RegistrationStatus;

public record RegistrationResponse(String journeyId, RegistrationStatus status) { }
