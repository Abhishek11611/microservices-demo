package com.example.demo.service.security;

import com.example.demo.exceptions.BadRequestException;
import com.example.demo.exceptions.NotFoundException;
import com.example.demo.exceptions.UnauthorisedException;
import com.example.demo.dtos.authentication.SendOTPDTO;
import com.example.demo.dtos.authentication.TokenResponseDTO;
import com.example.demo.dtos.authentication.VerifyOtpRequest;
import com.example.demo.dtos.authentication.VerifyPasswordRequest;
import com.example.demo.entities.auth.RefreshToken;
import com.example.demo.entities.users.AuthUser;
import com.example.demo.entities.users.Role;
import com.example.demo.entities.users.UserRoles;
import com.example.demo.enums.RecipientType;
import com.example.demo.exceptions.LimitExceededException;
import com.example.demo.repositories.auth.RefreshTokenRepository;
import com.example.demo.repositories.users.AuthUsersRepository;
import com.example.demo.service.onboard.OtpService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthUsersRepository authUsersRepository;

    @Mock
    private OtpService otpService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private HttpServletRequest servletRequest;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "ACCESS_TOKEN_TTL_MIN", 15L);
        ReflectionTestUtils.setField(jwtService, "REFRESH_TOKEN_TTL_MIN", 7L);
        lenient().when(jwtService.generateAccessToken(anyString(), anyString(), anyLong(), anyString()))
                .thenReturn("access-token");
        lenient().when(servletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
    }

    @Test
    void sendOtp_withEmailRecipient_sendsOtpToUsersMobileNumber() {
        SendOTPDTO request = new SendOTPDTO("john@example.com", RecipientType.EMAIL);
        AuthUser user = userWithRole("john@example.com", "9999999999", "USER");
        when(authUsersRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        String result = authenticationService.sendOtp(request);

        assertThat(result).isEqualTo("OTP Send Successfully");
        verify(otpService).sendOTP("9999999999");
        verify(authUsersRepository, never()).findByMobileNumber(anyString());
    }

    @Test
    void sendOtp_withPhoneRecipient_sendsOtpToRecipient() {
        SendOTPDTO request = new SendOTPDTO("9999999999", RecipientType.PHONE);
        AuthUser user = userWithRole("john@example.com", "9999999999", "USER");
        when(authUsersRepository.findByMobileNumber("9999999999")).thenReturn(Optional.of(user));

        String result = authenticationService.sendOtp(request);

        assertThat(result).isEqualTo("OTP Send Successfully");
        verify(otpService).sendOTP("9999999999");
        verify(authUsersRepository, never()).findByEmail(anyString());
    }

    @Test
    void sendOtp_whenUserDoesNotExist_throwsNotFoundException() {
        SendOTPDTO request = new SendOTPDTO("missing@example.com", RecipientType.EMAIL);
        when(authUsersRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.sendOtp(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("missing@example.com");

        verifyNoInteractions(otpService);
    }

    @Test
    void sendOtp_withNullRecipientType_throwsNullPointerException() {
        SendOTPDTO request = new SendOTPDTO("john@example.com", null);

        assertThatThrownBy(() -> authenticationService.sendOtp(request))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void verifyOTP_withValidOtp_returnsAccessAndRefreshTokens() throws Exception {
        VerifyOtpRequest request = new VerifyOtpRequest("9999999999", "123456");
        AuthUser user = userWithRole("john@example.com", "9999999999", "USER");
        when(otpService.verifyOtp("9999999999", "123456")).thenReturn(true);
        when(authUsersRepository.findByMobileNumber("9999999999")).thenReturn(Optional.of(user));

        TokenResponseDTO result = authenticationService.verifyOTP(request, servletRequest);

        assertThat(result.getAccessToken()).isEqualTo("access-token");
        assertThat(result.getRefreshToken()).isNotBlank();
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        assertThat(result.getExpiresAt()).isAfter(LocalDateTime.now());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void verifyOTP_withInvalidOtp_throwsBadRequestException() {
        VerifyOtpRequest request = new VerifyOtpRequest("9999999999", "000000");
        when(otpService.verifyOtp("9999999999", "000000")).thenReturn(false);

        assertThatThrownBy(() -> authenticationService.verifyOTP(request, servletRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("OTP is Invalid");

        verifyNoInteractions(authUsersRepository, refreshTokenRepository);
    }

    @Test
    void verifyOTP_whenOtpServiceThrows_propagatesException() {
        VerifyOtpRequest request = new VerifyOtpRequest("9999999999", "123456");
        when(otpService.verifyOtp("9999999999", "123456"))
                .thenThrow(new LimitExceededException("Maximum OTP verification attempts exceeded"));

        assertThatThrownBy(() -> authenticationService.verifyOTP(request, servletRequest))
                .isInstanceOf(LimitExceededException.class)
                .hasMessage("Maximum OTP verification attempts exceeded");
    }

    @Test
    void verifyOTP_whenUserDoesNotExist_throwsNotFoundException() {
        VerifyOtpRequest request = new VerifyOtpRequest("9999999999", "123456");
        when(otpService.verifyOtp("9999999999", "123456")).thenReturn(true);
        when(authUsersRepository.findByMobileNumber("9999999999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.verifyOTP(request, servletRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("9999999999");
    }

    @Test
    void verifyPassword_withCorrectPassword_returnsTokens() throws Exception {
        VerifyPasswordRequest request = new VerifyPasswordRequest("9999999999", "Secret123!");
        AuthUser user = userWithRole("john@example.com", "9999999999", "USER");
        when(authUsersRepository.findByMobileNumber("9999999999")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Secret123!", "encoded-password")).thenReturn(true);

        TokenResponseDTO result = authenticationService.verifyPassword(request, servletRequest);

        assertThat(result.getAccessToken()).isEqualTo("access-token");
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void verifyPassword_withIncorrectPassword_throwsUnauthorisedException() {
        VerifyPasswordRequest request = new VerifyPasswordRequest("9999999999", "wrong");
        AuthUser user = userWithRole("john@example.com", "9999999999", "USER");
        when(authUsersRepository.findByMobileNumber("9999999999")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> authenticationService.verifyPassword(request, servletRequest))
                .isInstanceOf(UnauthorisedException.class)
                .hasMessage("Incorrect Password");

        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    void verifyPassword_whenUserDoesNotExist_throwsNotFoundException() {
        VerifyPasswordRequest request = new VerifyPasswordRequest("9999999999", "Secret123!");
        when(authUsersRepository.findByMobileNumber("9999999999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.verifyPassword(request, servletRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void verifyPassword_whenUserHasNoRole_throwsNotFoundException() {
        VerifyPasswordRequest request = new VerifyPasswordRequest("9999999999", "Secret123!");
        AuthUser user = userWithRole("john@example.com", "9999999999", "USER");
        user.setUserRoles(new ArrayList<>());
        when(authUsersRepository.findByMobileNumber("9999999999")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Secret123!", "encoded-password")).thenReturn(true);

        assertThatThrownBy(() -> authenticationService.verifyPassword(request, servletRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Role Not Found");
    }

    @Test
    void refreshToken_withMissingHeader_throwsNotFoundException() {
        when(servletRequest.getHeader("Refresh-Token")).thenReturn(null);
        when(refreshTokenRepository.findByToken(null)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.refreshToken(servletRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Refresh Token Not Found");

        verify(refreshTokenRepository).findByToken(null);
    }

    @Test
    void refreshToken_whenTokenDoesNotExist_throwsNotFoundException() {
        when(servletRequest.getHeader("Refresh-Token")).thenReturn("unknown");
        when(refreshTokenRepository.findByToken("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.refreshToken(servletRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Refresh Token Not Found");
    }

    @Test
    void refreshToken_whenTokenIsExpired_throwsUnauthorisedException() {
        RefreshToken expiredToken = new RefreshToken();
        expiredToken.setToken("expired");
        expiredToken.setUserId(1L);
        expiredToken.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        when(servletRequest.getHeader("Refresh-Token")).thenReturn("expired");
        when(refreshTokenRepository.findByToken("expired")).thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> authenticationService.refreshToken(servletRequest))
                .isInstanceOf(UnauthorisedException.class)
                .hasMessage("Refresh token not found");
    }

    @Test
    void refreshToken_whenTokenIsRevoked_throwsUnauthorisedException() {
        RefreshToken revokedToken = new RefreshToken();
        revokedToken.setToken("revoked");
        revokedToken.setUserId(1L);
        revokedToken.setExpiresAt(LocalDateTime.now().plusDays(1));
        revokedToken.setRevokedAt(LocalDateTime.now().minusHours(1));
        when(servletRequest.getHeader("Refresh-Token")).thenReturn("revoked");
        when(refreshTokenRepository.findByToken("revoked")).thenReturn(Optional.of(revokedToken));

        assertThatThrownBy(() -> authenticationService.refreshToken(servletRequest))
                .isInstanceOf(UnauthorisedException.class)
                .hasMessage("Refresh token not found");
    }

    @Test
    void refreshToken_withValidToken_reusesRefreshTokenWhenShuffleIsNotRequired() throws Exception {
        RefreshToken existingToken = new RefreshToken();
        existingToken.setToken("existing-refresh-token");
        existingToken.setUserId(1L);
        existingToken.setExpiresAt(LocalDateTime.now().plusDays(1));
        existingToken.setCreatedAt(LocalDateTime.now());
        AuthUser user = userWithRole("john@example.com", "9999999999", "USER");
        when(servletRequest.getHeader("Refresh-Token")).thenReturn("existing-refresh-token");
        when(refreshTokenRepository.findByToken("existing-refresh-token")).thenReturn(Optional.of(existingToken));
        when(authUsersRepository.findById(1L)).thenReturn(Optional.of(user));

        TokenResponseDTO result = authenticationService.refreshToken(servletRequest);

        assertThat(result.getRefreshToken()).isEqualTo("existing-refresh-token");
        assertThat(result.getAccessToken()).isEqualTo("access-token");
        assertThat(existingToken.getExpiresAt()).isAfter(LocalDateTime.now());
        verify(refreshTokenRepository).save(existingToken);
    }

    @Test
    void refreshToken_withValidToken_createsNewRefreshTokenWhenShuffleIsRequired() throws Exception {
        RefreshToken existingToken = mock(RefreshToken.class);
        when(existingToken.getUserId()).thenReturn(1L);
        when(existingToken.isExpired()).thenReturn(false);
        when(existingToken.isRevokedAt()).thenReturn(false);
        when(existingToken.shouldShuffleToken()).thenReturn(true);
        AuthUser user = userWithRole("john@example.com", "9999999999", "USER");
        when(servletRequest.getHeader("Refresh-Token")).thenReturn("old-token");
        when(refreshTokenRepository.findByToken("old-token")).thenReturn(Optional.of(existingToken));
        when(authUsersRepository.findById(1L)).thenReturn(Optional.of(user));

        TokenResponseDTO result = authenticationService.refreshToken(servletRequest);

        ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(tokenCaptor.capture());
        RefreshToken savedToken = tokenCaptor.getValue();
        assertThat(result.getRefreshToken()).isEqualTo(savedToken.getToken());
        assertThat(savedToken.getToken()).isNotEqualTo("old-token");
        assertThat(savedToken.getUserId()).isEqualTo(1L);
        assertThat(savedToken.getCreatedByIpAddress()).isEqualTo("127.0.0.1");
        assertThat(savedToken.getExpiresAt()).isAfter(LocalDateTime.now());
    }

    @Test
    void testAccess_returnsExpectedMessage() {
        assertThat(authenticationService.testAccess()).isEqualTo("Hi Admin");
    }

    private AuthUser userWithRole(String email, String mobileNumber, String roleName) {
        AuthUser user = new AuthUser();
        user.setId(1L);
        user.setUserCode("UABC123");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail(email);
        user.setMobileNumber(mobileNumber);
        user.setPasswordHash("encoded-password");
        Role role = new Role(1L, roleName);
        UserRoles userRole = new UserRoles();
        userRole.setRole(role);
        userRole.setAuthUser(user);
        user.setUserRoles(new ArrayList<>(List.of(userRole)));
        return user;
    }
}
