package com.example.demo.controllers.auth;

import com.example.demo.exceptions.BadRequestException;
import com.example.demo.exceptions.GlobalExceptionHandler;
import com.example.demo.exceptions.NotFoundException;
import com.example.demo.exceptions.UnauthorisedException;
import com.example.demo.dtos.authentication.SendOTPDTO;
import com.example.demo.dtos.authentication.TokenResponseDTO;
import com.example.demo.dtos.authentication.VerifyOtpRequest;
import com.example.demo.dtos.authentication.VerifyPasswordRequest;
import com.example.demo.enums.RecipientType;
import com.example.demo.exceptions.LimitExceededException;
import com.example.demo.exceptions.TokenGenerationException;
import com.example.demo.service.security.AuthenticationService;
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

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void sendOtp_returnsOkWithServiceMessage() throws Exception {
        SendOTPDTO request = new SendOTPDTO("john@example.com", RecipientType.EMAIL);
        when(authenticationService.sendOtp(any(SendOTPDTO.class))).thenReturn("OTP Send Successfully");

        mockMvc.perform(post("/api/v1/auth/send-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("OTP Send Successfully"))
                .andExpect(jsonPath("$.message").value("Send OTP successfully"))
                .andExpect(jsonPath("$.status").value(true));

        verify(authenticationService).sendOtp(any(SendOTPDTO.class));
    }

    @Test
    void verifyOtp_returnsOkWithTokens() throws Exception {
        VerifyOtpRequest request = new VerifyOtpRequest("9999999999", "123456");
        TokenResponseDTO response = tokenResponse();
        when(authenticationService.verifyOTP(any(VerifyOtpRequest.class), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.data.email").value("john@example.com"))
                .andExpect(jsonPath("$.message").value("verify OTP successfully"))
                .andExpect(jsonPath("$.status").value(true));

        verify(authenticationService).verifyOTP(any(VerifyOtpRequest.class), any());
    }

    @Test
    void refreshToken_returnsOkWithTokens() throws Exception {
        when(authenticationService.refreshToken(any())).thenReturn(tokenResponse());

        mockMvc.perform(get("/api/v1/auth/refresh-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.message").value("Token refreshed successfully"))
                .andExpect(jsonPath("$.status").value(true));

        verify(authenticationService).refreshToken(any());
    }

    @Test
    void verifyPassword_returnsOkWithTokens() throws Exception {
        VerifyPasswordRequest request = new VerifyPasswordRequest("9999999999", "Secret123!");
        when(authenticationService.verifyPassword(any(VerifyPasswordRequest.class), any()))
                .thenReturn(tokenResponse());

        mockMvc.perform(post("/api/v1/auth/verify-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.message").value("Password Verified successfully"))
                .andExpect(jsonPath("$.status").value(true));

        verify(authenticationService).verifyPassword(any(VerifyPasswordRequest.class), any());
    }

    @Test
    void sendOtp_whenServiceThrowsBadRequest_returnsBadRequest() throws Exception {
        SendOTPDTO request = new SendOTPDTO("john@example.com", RecipientType.EMAIL);
        when(authenticationService.sendOtp(any(SendOTPDTO.class)))
                .thenThrow(new BadRequestException("bad request"));

        mockMvc.perform(post("/api/v1/auth/send-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("bad request"))
                .andExpect(jsonPath("$.status").value(false));
    }

    @Test
    void verifyOtp_whenServiceThrowsNotFound_returnsNotFound() throws Exception {
        VerifyOtpRequest request = new VerifyOtpRequest("9999999999", "123456");
        when(authenticationService.verifyOTP(any(VerifyOtpRequest.class), any()))
                .thenThrow(new NotFoundException("user not found"));

        mockMvc.perform(post("/api/v1/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("user not found"))
                .andExpect(jsonPath("$.status").value(false));
    }

    @Test
    void verifyPassword_whenServiceThrowsUnauthorised_returnsUnauthorised() throws Exception {
        VerifyPasswordRequest request = new VerifyPasswordRequest("9999999999", "wrong");
        when(authenticationService.verifyPassword(any(VerifyPasswordRequest.class), any()))
                .thenThrow(new UnauthorisedException("invalid password"));

        mockMvc.perform(post("/api/v1/auth/verify-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("invalid password"))
                .andExpect(jsonPath("$.status").value(false));
    }

    @Test
    void refreshToken_whenServiceThrowsLimitExceeded_returnsTooManyRequests() throws Exception {
        when(authenticationService.refreshToken(any()))
                .thenThrow(new LimitExceededException("too many requests"));

        mockMvc.perform(get("/api/v1/auth/refresh-token"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message").value("too many requests"))
                .andExpect(jsonPath("$.status").value(false));
    }

    @Test
    void sendOtp_whenServiceThrowsTokenGenerationException_returnsInternalServerError() throws Exception {
        SendOTPDTO request = new SendOTPDTO("john@example.com", RecipientType.EMAIL);
        when(authenticationService.sendOtp(any(SendOTPDTO.class)))
                .thenThrow(new TokenGenerationException("token generation failed"));

        mockMvc.perform(post("/api/v1/auth/send-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("token generation failed"))
                .andExpect(jsonPath("$.status").value(false));
    }

    private TokenResponseDTO tokenResponse() {
        return new TokenResponseDTO(
                "refresh-token",
                "access-token",
                LocalDateTime.now().plusMinutes(15),
                "john@example.com"
        );
    }
}
