package com.example.demo.entities.auth;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name="refresh_tokens")
public class RefreshToken {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long refreshTokenId;

    @Column(name = "token")
    private String token;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name="created_by_ip_address")
    private String createdByIpAddress;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name="updated_at")
    private LocalDateTime updatedAt;

    public RefreshToken() {
    }

    public RefreshToken(Long refreshTokenId, String token, Long userId, LocalDateTime expiresAt, LocalDateTime revokedAt, String createdByIpAddress, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.refreshTokenId = refreshTokenId;
        this.token = token;
        this.userId = userId;
        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
        this.createdByIpAddress = createdByIpAddress;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getRefreshTokenId() {
        return refreshTokenId;
    }

    public void setRefreshTokenId(Long refreshTokenId) {
        this.refreshTokenId = refreshTokenId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(LocalDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }

    public String getCreatedByIpAddress() {
        return createdByIpAddress;
    }

    public void setCreatedByIpAddress(String createdByIpAddress) {
        this.createdByIpAddress = createdByIpAddress;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean shouldShuffleToken() {
        return Duration.between(LocalDateTime.now(),createdAt).getSeconds() > 3.5 * 24 * 60 * 60;
    }
}
