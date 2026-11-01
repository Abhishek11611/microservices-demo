package com.example.demo.controllers.auth;

import com.example.demo.exceptions.GlobalExceptionHandler;
import com.example.demo.exceptions.UnauthorisedException;
import com.example.demo.config.SecurityConfig;
import com.example.demo.dtos.authentication.SendOTPDTO;
import com.example.demo.enums.RecipientType;
import com.example.demo.exceptions.CustomAccessDeniedHandler;
import com.example.demo.service.security.AuthenticationService;
import com.example.demo.service.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWTClaimsSet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {TestController.class, AuthController.class})
@Import({SecurityConfig.class, CustomAccessDeniedHandler.class, GlobalExceptionHandler.class})
class TestControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void publicAuthEndpoint_withoutToken_isAllowed() throws Exception {
        SendOTPDTO request = new SendOTPDTO("john@example.com", RecipientType.EMAIL);
        when(authenticationService.sendOtp(any(SendOTPDTO.class))).thenReturn("OTP Send Successfully");

        mockMvc.perform(post("/api/v1/auth/send-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpoint_withoutToken_isForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/test/check-access"))
                .andExpect(status().isForbidden());
    }

    @Test
    void protectedEndpoint_withManagerRole_isAllowed() throws Exception {
        when(jwtService.parseAndValidate("manager-token")).thenReturn(claims("MANAGER"));
        when(authenticationService.testAccess()).thenReturn("Hi Admin");

        mockMvc.perform(get("/api/v1/test/check-access")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer manager-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hi Admin"));
    }

    @Test
    void protectedEndpoint_withMultipleRolesIncludingManager_isAllowed() throws Exception {
        when(jwtService.parseAndValidate("multi-role-token")).thenReturn(claims("USER, MANAGER"));
        when(authenticationService.testAccess()).thenReturn("Hi Admin");

        mockMvc.perform(get("/api/v1/test/check-access")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer multi-role-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hi Admin"));
    }

    @Test
    void protectedEndpoint_withUserRole_isForbidden() throws Exception {
        when(jwtService.parseAndValidate("user-token")).thenReturn(claims("USER"));

        mockMvc.perform(get("/api/v1/test/check-access")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer user-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void protectedEndpoint_withInvalidToken_isForbidden() throws Exception {
        when(jwtService.parseAndValidate(anyString()))
                .thenThrow(new UnauthorisedException("invalid token"));

        mockMvc.perform(get("/api/v1/test/check-access")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token"))
                .andExpect(status().isForbidden());
    }

    private JWTClaimsSet claims(String roles) {
        return new JWTClaimsSet.Builder()
                .subject("john@example.com")
                .claim("roles", roles)
                .build();
    }
}
