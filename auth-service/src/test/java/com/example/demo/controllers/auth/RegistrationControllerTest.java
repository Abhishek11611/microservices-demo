package com.example.demo.controllers.auth;

import com.example.demo.exceptions.BadRequestException;
import com.example.demo.exceptions.GlobalExceptionHandler;
import com.example.demo.exceptions.NotFoundException;
import com.example.demo.dtos.registration.PasswordRequest;
import com.example.demo.dtos.registration.RegistrationOTPRequest;
import com.example.demo.dtos.registration.RegistrationPersonalDetailsRequest;
import com.example.demo.dtos.registration.RegistrationResponse;
import com.example.demo.enums.RegistrationStatus;
import com.example.demo.service.onboard.UsersRegistrationService;
import com.example.demo.service.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RegistrationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UsersRegistrationService usersRegistrationService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void registrationPersonalDetails_returnsCreated() throws Exception {
        RegistrationPersonalDetailsRequest request = personalDetailsRequest();
        when(usersRegistrationService.registrationPersonalDetails(any(RegistrationPersonalDetailsRequest.class)))
                .thenReturn(new RegistrationResponse("journey-1", RegistrationStatus.PERSONAL_INFORMATION));

        mockMvc.perform(post("/api/v1/registrations/personal-details")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.journeyId").value("journey-1"))
                .andExpect(jsonPath("$.data.status").value("PERSONAL_INFORMATION"))
                .andExpect(jsonPath("$.message").value("Personal Information successfully"))
                .andExpect(jsonPath("$.status").value(true));

        verify(usersRegistrationService).registrationPersonalDetails(any(RegistrationPersonalDetailsRequest.class));
    }

    @Test
    void registrationVerifyOTP_returnsCreated() throws Exception {
        RegistrationOTPRequest request = new RegistrationOTPRequest("journey-1", "123456");
        when(usersRegistrationService.registrationVerifyOtp(any(RegistrationOTPRequest.class)))
                .thenReturn(new RegistrationResponse("journey-1", RegistrationStatus.OTP_VERIFIED));

        mockMvc.perform(post("/api/v1/registrations/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.journeyId").value("journey-1"))
                .andExpect(jsonPath("$.data.status").value("OTP_VERIFIED"))
                .andExpect(jsonPath("$.message").value("verify OTP successfully"))
                .andExpect(jsonPath("$.status").value(true));

        verify(usersRegistrationService).registrationVerifyOtp(any(RegistrationOTPRequest.class));
    }

    @Test
    void registrationPassword_returnsCreated() throws Exception {
        PasswordRequest request = new PasswordRequest("journey-1", "Secret123!", "Secret123!");
        when(usersRegistrationService.registrationPassword(any(PasswordRequest.class)))
                .thenReturn(new RegistrationResponse("journey-1", RegistrationStatus.PASSWORD_SET));

        mockMvc.perform(post("/api/v1/registrations/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.journeyId").value("journey-1"))
                .andExpect(jsonPath("$.data.status").value("PASSWORD_SET"))
                .andExpect(jsonPath("$.message").value("Password Created successfully"))
                .andExpect(jsonPath("$.status").value(true));

        verify(usersRegistrationService).registrationPassword(any(PasswordRequest.class));
    }

    @Test
    void registrationPersonalDetails_whenServiceThrowsBadRequest_returnsBadRequest() throws Exception {
        when(usersRegistrationService.registrationPersonalDetails(any(RegistrationPersonalDetailsRequest.class)))
                .thenThrow(new BadRequestException("email already exists"));

        mockMvc.perform(post("/api/v1/registrations/personal-details")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(personalDetailsRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("email already exists"))
                .andExpect(jsonPath("$.status").value(false));
    }

    @Test
    void registrationVerifyOTP_whenServiceThrowsNotFound_returnsNotFound() throws Exception {
        RegistrationOTPRequest request = new RegistrationOTPRequest("unknown", "123456");
        when(usersRegistrationService.registrationVerifyOtp(any(RegistrationOTPRequest.class)))
                .thenThrow(new NotFoundException("registration session expired"));

        mockMvc.perform(post("/api/v1/registrations/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("registration session expired"))
                .andExpect(jsonPath("$.status").value(false));
    }

    @Test
    void registrationPassword_whenServiceThrowsBadRequest_returnsBadRequest() throws Exception {
        PasswordRequest request = new PasswordRequest("journey-1", "Secret123!", "Different123!");
        when(usersRegistrationService.registrationPassword(any(PasswordRequest.class)))
                .thenThrow(new BadRequestException("passwords do not match"));

        mockMvc.perform(post("/api/v1/registrations/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("passwords do not match"))
                .andExpect(jsonPath("$.status").value(false));
    }

    private RegistrationPersonalDetailsRequest personalDetailsRequest() {
        return new RegistrationPersonalDetailsRequest(
                "John",
                "Doe",
                "john@example.com",
                "9999999999",
                LocalDate.of(1990, 1, 1)
        );
    }
}
