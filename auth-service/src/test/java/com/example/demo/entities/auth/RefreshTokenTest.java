package com.example.demo.entities.auth;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RefreshTokenTest {

    @Test
    void isExpired_returnsTrueForPastExpiry() {
        RefreshToken token = new RefreshToken();
        token.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        assertThat(token.isExpired()).isTrue();
    }

    @Test
    void isExpired_returnsFalseForFutureExpiry() {
        RefreshToken token = new RefreshToken();
        token.setExpiresAt(LocalDateTime.now().plusMinutes(1));

        assertThat(token.isExpired()).isFalse();
    }

    @Test
    void isRevokedAt_returnsTrueWhenRevokedAtIsSet() {
        RefreshToken token = new RefreshToken();
        token.setRevokedAt(LocalDateTime.now());

        assertThat(token.isRevokedAt()).isTrue();
    }

    @Test
    void isRevokedAt_returnsFalseWhenRevokedAtIsNull() {
        RefreshToken token = new RefreshToken();

        assertThat(token.isRevokedAt()).isFalse();
    }

    @Test
    void shouldShuffleToken_returnsFalseForRecentlyCreatedToken() {
        RefreshToken token = new RefreshToken();
        token.setCreatedAt(LocalDateTime.now());

        assertThat(token.shouldShuffleToken()).isFalse();
    }

    @Test
    void shouldShuffleToken_returnsTrueWhenCreatedAtIsMoreThanThreeAndAHalfDaysInTheFuture() {
        RefreshToken token = new RefreshToken();
        token.setCreatedAt(LocalDateTime.now().plusDays(4));

        assertThat(token.shouldShuffleToken()).isTrue();
    }

    @Test
    void shouldShuffleToken_returnsFalseForOldTokenWithCurrentImplementation() {
        RefreshToken token = new RefreshToken();
        token.setCreatedAt(LocalDateTime.now().minusDays(10));

        assertThat(token.shouldShuffleToken()).isFalse();
    }

    @Test
    void shouldShuffleToken_withNullCreatedAt_throwsNullPointerException() {
        RefreshToken token = new RefreshToken();

        assertThatThrownBy(token::shouldShuffleToken)
                .isInstanceOf(NullPointerException.class);
    }
}
