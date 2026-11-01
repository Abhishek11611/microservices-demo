package com.example.demo.service.onboard;

import com.example.demo.exceptions.LimitExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OtpServiceImplTest {

    private static final String IDENTIFIER = "user@example.com";

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private OtpServiceImpl otpService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void sendOTP_whenAllowed_storesSixDigitOtpAndCooldown() {
        when(redisTemplate.hasKey("otp:cooldown:" + IDENTIFIER)).thenReturn(false);
        when(valueOperations.increment("otp:send_count:" + IDENTIFIER)).thenReturn(1L);

        otpService.sendOTP(IDENTIFIER);

        ArgumentCaptor<Object> otpCaptor = ArgumentCaptor.forClass(Object.class);
        verify(valueOperations).set(eq("otp:" + IDENTIFIER), otpCaptor.capture(), eq(Duration.ofMinutes(5)));
        assertThat(otpCaptor.getValue().toString()).matches("\\d{6}");
        verify(valueOperations).set("otp:cooldown:" + IDENTIFIER, "1", Duration.ofMinutes(1));
        verify(redisTemplate).expire("otp:send_count:" + IDENTIFIER, Duration.ofMinutes(15));
    }

    @Test
    void sendOTP_whenCooldownIsActive_throwsLimitExceededException() {
        when(redisTemplate.hasKey("otp:cooldown:" + IDENTIFIER)).thenReturn(true);

        assertThatThrownBy(() -> otpService.sendOTP(IDENTIFIER))
                .isInstanceOf(LimitExceededException.class)
                .hasMessageContaining("60s");

        verify(valueOperations, never()).increment(anyString());
        verify(valueOperations, never()).set(anyString(), any(), any(Duration.class));
    }

    @Test
    void sendOTP_whenCooldownKeyReturnsNull_treatsCooldownAsInactive() {
        when(redisTemplate.hasKey("otp:cooldown:" + IDENTIFIER)).thenReturn(null);
        when(valueOperations.increment("otp:send_count:" + IDENTIFIER)).thenReturn(1L);

        otpService.sendOTP(IDENTIFIER);

        verify(valueOperations).set(eq("otp:" + IDENTIFIER), anyString(), eq(Duration.ofMinutes(5)));
    }

    @Test
    void sendOTP_whenSendCountExceedsMaximum_throwsLimitExceededException() {
        when(redisTemplate.hasKey("otp:cooldown:" + IDENTIFIER)).thenReturn(false);
        when(valueOperations.increment("otp:send_count:" + IDENTIFIER)).thenReturn(6L);

        assertThatThrownBy(() -> otpService.sendOTP(IDENTIFIER))
                .isInstanceOf(LimitExceededException.class)
                .hasMessage("Maximum OTP requests exceeded");

        verify(valueOperations, never()).set(anyString(), any(), any(Duration.class));
    }

    @Test
    void sendOTP_whenSendCountIsNotFirst_doesNotSetSendCountExpiry() {
        when(redisTemplate.hasKey("otp:cooldown:" + IDENTIFIER)).thenReturn(false);
        when(valueOperations.increment("otp:send_count:" + IDENTIFIER)).thenReturn(2L);

        otpService.sendOTP(IDENTIFIER);

        verify(redisTemplate, never()).expire(anyString(), any(Duration.class));
        verify(valueOperations).set(eq("otp:" + IDENTIFIER), anyString(), eq(Duration.ofMinutes(5)));
    }

    @Test
    void sendOTP_whenIncrementReturnsNull_stillStoresOtp() {
        when(redisTemplate.hasKey("otp:cooldown:" + IDENTIFIER)).thenReturn(false);
        when(valueOperations.increment("otp:send_count:" + IDENTIFIER)).thenReturn(null);

        otpService.sendOTP(IDENTIFIER);

        verify(redisTemplate, never()).expire(anyString(), any(Duration.class));
        verify(valueOperations).set(eq("otp:" + IDENTIFIER), anyString(), eq(Duration.ofMinutes(5)));
    }

    @Test
    void verifyOtp_withNullOtp_returnsFalse() {
        assertThat(otpService.verifyOtp(IDENTIFIER, null)).isFalse();

        verifyNoInteractions(valueOperations);
    }

    @Test
    void verifyOtp_withBlankOtp_returnsFalse() {
        assertThat(otpService.verifyOtp(IDENTIFIER, "   ")).isFalse();

        verifyNoInteractions(valueOperations);
    }

    @Test
    void verifyOtp_whenNoStoredOtpExists_throwsRuntimeException() {
        when(valueOperations.get("otp:" + IDENTIFIER)).thenReturn(null);

        assertThatThrownBy(() -> otpService.verifyOtp(IDENTIFIER, "123456"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Please Send OTP.");
    }

    @Test
    void verifyOtp_withMatchingOtp_deletesOtpAndAttemptsAndReturnsTrue() {
        when(valueOperations.get("otp:" + IDENTIFIER)).thenReturn("123456");

        Boolean result = otpService.verifyOtp(IDENTIFIER, " 123456 ");

        assertThat(result).isTrue();
        verify(redisTemplate).delete("otp:" + IDENTIFIER);
        verify(redisTemplate).delete("otp:verify_attempts:" + IDENTIFIER);
        verify(valueOperations, never()).increment(anyString());
    }

    @Test
    void verifyOtp_withMatchingNumericStoredOtp_returnsTrue() {
        when(valueOperations.get("otp:" + IDENTIFIER)).thenReturn(123456);

        assertThat(otpService.verifyOtp(IDENTIFIER, "123456")).isTrue();
    }

    @Test
    void verifyOtp_withWrongOtp_incrementsAttemptsAndReturnsFalse() {
        when(valueOperations.get("otp:" + IDENTIFIER)).thenReturn("123456");
        when(valueOperations.increment("otp:verify_attempts:" + IDENTIFIER)).thenReturn(1L);

        Boolean result = otpService.verifyOtp(IDENTIFIER, "000000");

        assertThat(result).isFalse();
        verify(redisTemplate).expire("otp:verify_attempts:" + IDENTIFIER, Duration.ofMinutes(5));
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void verifyOtp_withWrongOtpOnLaterAttempt_doesNotResetAttemptExpiry() {
        when(valueOperations.get("otp:" + IDENTIFIER)).thenReturn("123456");
        when(valueOperations.increment("otp:verify_attempts:" + IDENTIFIER)).thenReturn(2L);

        Boolean result = otpService.verifyOtp(IDENTIFIER, "000000");

        assertThat(result).isFalse();
        verify(redisTemplate, never()).expire(anyString(), any(Duration.class));
    }

    @Test
    void verifyOtp_whenMaximumAttemptsExceeded_deletesKeysAndThrows() {
        when(valueOperations.get("otp:" + IDENTIFIER)).thenReturn("123456");
        when(valueOperations.increment("otp:verify_attempts:" + IDENTIFIER)).thenReturn(4L);

        assertThatThrownBy(() -> otpService.verifyOtp(IDENTIFIER, "000000"))
                .isInstanceOf(LimitExceededException.class)
                .hasMessage("Maximum OTP verification attempts exceeded");

        verify(redisTemplate).delete("otp:" + IDENTIFIER);
        verify(redisTemplate).delete("otp:verify_attempts:" + IDENTIFIER);
    }

    @Test
    void verifyOtp_whenAttemptIncrementReturnsNull_returnsFalse() {
        when(valueOperations.get("otp:" + IDENTIFIER)).thenReturn("123456");
        when(valueOperations.increment("otp:verify_attempts:" + IDENTIFIER)).thenReturn(null);

        assertThat(otpService.verifyOtp(IDENTIFIER, "000000")).isFalse();

        verify(redisTemplate, never()).expire(anyString(), any(Duration.class));
        verify(redisTemplate, never()).delete(anyString());
    }
}
