package com.example.demo.dtos.authentication;

import java.time.LocalDateTime;

public class TokenResponseDTO {

    private String refreshToken;
    private String accessToken;
    private LocalDateTime expiresAt;
    private String email;

    public TokenResponseDTO() {
    }

    public TokenResponseDTO(String refreshToken, String accessToken, LocalDateTime expiresAt, String email) {
        this.refreshToken = refreshToken;
        this.accessToken = accessToken;
        this.expiresAt = expiresAt;
        this.email = email;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
