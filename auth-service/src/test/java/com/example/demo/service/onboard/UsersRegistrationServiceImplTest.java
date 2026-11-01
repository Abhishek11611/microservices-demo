package com.example.demo.service.onboard;

import com.example.demo.exceptions.BadRequestException;
import com.example.demo.exceptions.NotFoundException;
import com.example.demo.dtos.registration.PasswordRequest;
import com.example.demo.dtos.registration.RegistrationDTO;
import com.example.demo.dtos.registration.RegistrationOTPRequest;
import com.example.demo.dtos.registration.RegistrationPersonalDetailsRequest;
import com.example.demo.dtos.registration.RegistrationResponse;
import com.example.demo.enums.RegistrationStatus;
import com.example.demo.exceptions.LimitExceededException;
import com.example.demo.repositories.users.AuthUsersRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsersRegistrationServiceImplTest {

    private static final String JOURNEY_ID = "journey-1";
    private static final String EMAIL = "john@example.com";
    private static final String MOBILE = "9999999999";

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private OtpService otpService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthUsersRepository authUsersRepository;

    @Mock
    private UserRegistrationPersistenceServiceImpl userRegistrationPersistenceService;

    private UsersRegistrationServiceImpl registrationService;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        registrationService = new UsersRegistrationServiceImpl(
                redisTemplate,
                kafkaTemplate,
                otpService,
                objectMapper,
                passwordEncoder,
                authUsersRepository,
                userRegistrationPersistenceService
        );
    }

    @Test
    void registrationPersonalDetails_whenDataIsNew_storesJourneyAndSendsOtp() {
        stubNoDuplicateRegistration();
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(true);

        RegistrationResponse response = registrationService.registrationPersonalDetails(personalDetailsRequest());

        assertThat(response.status()).isEqualTo(RegistrationStatus.PERSONAL_INFORMATION);
        assertThat(response.journeyId()).isNotBlank();
        ArgumentCaptor<RegistrationDTO> dtoCaptor = ArgumentCaptor.forClass(RegistrationDTO.class);
        verify(valueOperations).set(startsWith("registration:"), dtoCaptor.capture(), eq(Duration.ofMinutes(10)));
        RegistrationDTO savedDto = dtoCaptor.getValue();
        assertThat(savedDto.getJourneyId()).isEqualTo(response.journeyId());
        assertThat(savedDto.getUserCode()).startsWith("U").hasSize(7);
        assertThat(savedDto.getFirstName()).isEqualTo("John");
        assertThat(savedDto.getLastName()).isEqualTo("Doe");
        assertThat(savedDto.getEmail()).isEqualTo(EMAIL);
        assertThat(savedDto.getMobileNumber()).isEqualTo(MOBILE);
        assertThat(savedDto.getDateOfBirth()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(savedDto.getStatus()).isEqualTo(RegistrationStatus.PERSONAL_INFORMATION);
        verify(valueOperations, org.mockito.Mockito.times(2))
                .setIfAbsent(anyString(), anyString(), any(Duration.class));
        verify(otpService).sendOTP(response.journeyId());
    }

    @Test
    void registrationPersonalDetails_whenUserCodeExists_throwsBadRequestException() {
        when(authUsersRepository.existsByUserCode(anyString())).thenReturn(true);

        assertThatThrownBy(() -> registrationService.registrationPersonalDetails(personalDetailsRequest()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("UserCode already exists Please Try Again.");

        verify(valueOperations, never()).setIfAbsent(anyString(), anyString(), any(Duration.class));
    }

    @Test
    void registrationPersonalDetails_whenEmailExists_throwsBadRequestException() {
        when(authUsersRepository.existsByUserCode(anyString())).thenReturn(false);
        when(authUsersRepository.existsByEmail(EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> registrationService.registrationPersonalDetails(personalDetailsRequest()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Email already exists.");

        verify(valueOperations, never()).setIfAbsent(anyString(), anyString(), any(Duration.class));
    }

    @Test
    void registrationPersonalDetails_whenMobileNumberExists_throwsBadRequestException() {
        when(authUsersRepository.existsByUserCode(anyString())).thenReturn(false);
        when(authUsersRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(authUsersRepository.existsByMobileNumber(MOBILE)).thenReturn(true);

        assertThatThrownBy(() -> registrationService.registrationPersonalDetails(personalDetailsRequest()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Mobile Number already exists.");

        verify(valueOperations, never()).setIfAbsent(anyString(), anyString(), any(Duration.class));
    }

    @Test
    void registrationPersonalDetails_whenEmailLockIsAlreadyHeld_throwsBadRequestException() {
        stubNoDuplicateRegistration();
        when(valueOperations.setIfAbsent(eq("email:" + EMAIL), anyString(), any(Duration.class)))
                .thenReturn(false);

        assertThatThrownBy(() -> registrationService.registrationPersonalDetails(personalDetailsRequest()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Email is already being used in another registration.");

        verify(valueOperations, never()).set(startsWith("registration:"), any(), any(Duration.class));
        verifyNoInteractions(otpService);
    }

    @Test
    void registrationPersonalDetails_whenMobileLockIsAlreadyHeld_throwsBadRequestException() {
        stubNoDuplicateRegistration();
        when(valueOperations.setIfAbsent(eq("email:" + EMAIL), anyString(), any(Duration.class)))
                .thenReturn(true);
        when(valueOperations.setIfAbsent(eq("mobileNumber:" + MOBILE), anyString(), any(Duration.class)))
                .thenReturn(false);

        assertThatThrownBy(() -> registrationService.registrationPersonalDetails(personalDetailsRequest()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Mobile Number is already being used in another registration.");

        verify(valueOperations, never()).set(startsWith("registration:"), any(), any(Duration.class));
        verifyNoInteractions(otpService);
    }

    @Test
    void registrationPersonalDetails_whenOtpSendingFails_propagatesException() {
        stubNoDuplicateRegistration();
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(true);
        org.mockito.Mockito.doThrow(new RuntimeException("otp provider down"))
                .when(otpService).sendOTP(anyString());

        assertThatThrownBy(() -> registrationService.registrationPersonalDetails(personalDetailsRequest()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("otp provider down");
    }

    @Test
    void registrationPersonalDetails_withNullRequest_throwsNullPointerException() {
        assertThatThrownBy(() -> registrationService.registrationPersonalDetails(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void registrationVerifyOtp_withValidOtp_updatesStatusAndPreservesTtl() {
        RegistrationDTO dto = registrationDto(RegistrationStatus.PERSONAL_INFORMATION);
        when(valueOperations.get("registration:" + JOURNEY_ID)).thenReturn(dto);
        when(redisTemplate.getExpire("registration:" + JOURNEY_ID, TimeUnit.SECONDS)).thenReturn(300L);
        when(otpService.verifyOtp(JOURNEY_ID, "123456")).thenReturn(true);

        RegistrationResponse response = registrationService.registrationVerifyOtp(
                new RegistrationOTPRequest(JOURNEY_ID, "123456")
        );

        assertThat(response.journeyId()).isEqualTo(JOURNEY_ID);
        assertThat(response.status()).isEqualTo(RegistrationStatus.OTP_VERIFIED);
        verify(valueOperations).set(
                eq("registration:" + JOURNEY_ID),
                any(RegistrationDTO.class),
                eq(300L),
                eq(TimeUnit.SECONDS)
        );
    }

    @Test
    void registrationVerifyOtp_whenSessionDoesNotExist_throwsNotFoundException() {
        when(valueOperations.get("registration:" + JOURNEY_ID)).thenReturn(null);

        assertThatThrownBy(() -> registrationService.registrationVerifyOtp(
                new RegistrationOTPRequest(JOURNEY_ID, "123456")))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Registration session is invalid or has expired");

        verifyNoInteractions(otpService);
    }

    @Test
    void registrationVerifyOtp_whenStatusIsNotPersonalInformation_throwsBadRequestException() {
        RegistrationDTO dto = registrationDto(RegistrationStatus.OTP_VERIFIED);
        when(valueOperations.get("registration:" + JOURNEY_ID)).thenReturn(dto);

        assertThatThrownBy(() -> registrationService.registrationVerifyOtp(
                new RegistrationOTPRequest(JOURNEY_ID, "123456")))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Please complete your personal information before verifying your OTP.");

        verifyNoInteractions(otpService);
    }

    @Test
    void registrationVerifyOtp_whenOtpIsInvalid_throwsBadRequestException() {
        RegistrationDTO dto = registrationDto(RegistrationStatus.PERSONAL_INFORMATION);
        when(valueOperations.get("registration:" + JOURNEY_ID)).thenReturn(dto);
        when(otpService.verifyOtp(JOURNEY_ID, "000000")).thenReturn(false);

        assertThatThrownBy(() -> registrationService.registrationVerifyOtp(
                new RegistrationOTPRequest(JOURNEY_ID, "000000")))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("The OTP you entered is invalid or has expired.");

        verify(valueOperations, never()).set(anyString(), any(), anyLong(), any(TimeUnit.class));
    }

    @Test
    void registrationVerifyOtp_whenOtpServiceThrows_propagatesException() {
        RegistrationDTO dto = registrationDto(RegistrationStatus.PERSONAL_INFORMATION);
        when(valueOperations.get("registration:" + JOURNEY_ID)).thenReturn(dto);
        when(otpService.verifyOtp(JOURNEY_ID, "123456"))
                .thenThrow(new LimitExceededException("Maximum OTP verification attempts exceeded"));

        assertThatThrownBy(() -> registrationService.registrationVerifyOtp(
                new RegistrationOTPRequest(JOURNEY_ID, "123456")))
                .isInstanceOf(LimitExceededException.class)
                .hasMessage("Maximum OTP verification attempts exceeded");
    }

    @Test
    void registrationPassword_whenOtpIsVerified_encodesPasswordAndPersistsUser() throws Exception {
        RegistrationDTO dto = registrationDto(RegistrationStatus.OTP_VERIFIED);
        when(valueOperations.get("registration:" + JOURNEY_ID)).thenReturn(dto);
        when(passwordEncoder.encode("Secret123!")).thenReturn("encoded-password");

        RegistrationResponse response = registrationService.registrationPassword(
                new PasswordRequest(JOURNEY_ID, "Secret123!", "Secret123!")
        );

        assertThat(response.journeyId()).isEqualTo(JOURNEY_ID);
        assertThat(response.status()).isEqualTo(RegistrationStatus.PASSWORD_SET);
        ArgumentCaptor<RegistrationDTO> dtoCaptor = ArgumentCaptor.forClass(RegistrationDTO.class);
        verify(userRegistrationPersistenceService).register(dtoCaptor.capture());
        RegistrationDTO persistedDto = dtoCaptor.getValue();
        assertThat(persistedDto.getPasswordHash()).isEqualTo("encoded-password");
        assertThat(persistedDto.getStatus()).isEqualTo(RegistrationStatus.PASSWORD_SET);
    }

    @Test
    void registrationPassword_whenPasswordsDoNotMatch_throwsBadRequestException() {
        assertThatThrownBy(() -> registrationService.registrationPassword(
                new PasswordRequest(JOURNEY_ID, "Secret123!", "Different123!")))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Passwords do not match");

        verifyNoInteractions(valueOperations, userRegistrationPersistenceService);
    }

    @Test
    void registrationPassword_whenSessionDoesNotExist_throwsNotFoundException() {
        when(valueOperations.get("registration:" + JOURNEY_ID)).thenReturn(null);

        assertThatThrownBy(() -> registrationService.registrationPassword(
                new PasswordRequest(JOURNEY_ID, "Secret123!", "Secret123!")))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Registration session is invalid or has expired.");

        verifyNoInteractions(userRegistrationPersistenceService);
    }

    @Test
    void registrationPassword_whenPasswordAlreadySet_throwsBadRequestException() {
        RegistrationDTO dto = registrationDto(RegistrationStatus.PASSWORD_SET);
        when(valueOperations.get("registration:" + JOURNEY_ID)).thenReturn(dto);

        assertThatThrownBy(() -> registrationService.registrationPassword(
                new PasswordRequest(JOURNEY_ID, "Secret123!", "Secret123!")))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Password has already been set.");

        verifyNoInteractions(userRegistrationPersistenceService);
    }

    @Test
    void registrationPassword_whenOtpIsNotVerified_throwsBadRequestException() {
        RegistrationDTO dto = registrationDto(RegistrationStatus.PERSONAL_INFORMATION);
        when(valueOperations.get("registration:" + JOURNEY_ID)).thenReturn(dto);

        assertThatThrownBy(() -> registrationService.registrationPassword(
                new PasswordRequest(JOURNEY_ID, "Secret123!", "Secret123!")))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Please complete your Verify OTP before Set password.");

        verifyNoInteractions(userRegistrationPersistenceService);
    }

    @Test
    void registrationPassword_whenPersistenceFails_propagatesException() {
        RegistrationDTO dto = registrationDto(RegistrationStatus.OTP_VERIFIED);
        when(valueOperations.get("registration:" + JOURNEY_ID)).thenReturn(dto);
        when(passwordEncoder.encode("Secret123!")).thenReturn("encoded-password");
        org.mockito.Mockito.doThrow(new RuntimeException("database down"))
                .when(userRegistrationPersistenceService).register(any(RegistrationDTO.class));

        assertThatThrownBy(() -> registrationService.registrationPassword(
                new PasswordRequest(JOURNEY_ID, "Secret123!", "Secret123!")))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("database down");
    }

    @Test
    void registrationPassword_withNullPassword_throwsNullPointerException() {
        assertThatThrownBy(() -> registrationService.registrationPassword(
                new PasswordRequest(JOURNEY_ID, null, "Secret123!")))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void registrationPassword_withNullRequest_throwsNullPointerException() {
        assertThatThrownBy(() -> registrationService.registrationPassword(null))
                .isInstanceOf(NullPointerException.class);
    }

    private void stubNoDuplicateRegistration() {
        when(authUsersRepository.existsByUserCode(anyString())).thenReturn(false);
        when(authUsersRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(authUsersRepository.existsByMobileNumber(MOBILE)).thenReturn(false);
    }

    private RegistrationPersonalDetailsRequest personalDetailsRequest() {
        return new RegistrationPersonalDetailsRequest(
                "John", "Doe", EMAIL, MOBILE, LocalDate.of(1990, 1, 1)
        );
    }

    private RegistrationDTO registrationDto(RegistrationStatus status) {
        RegistrationDTO dto = new RegistrationDTO();
        dto.setJourneyId(JOURNEY_ID);
        dto.setUserCode("UABC123");
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setEmail(EMAIL);
        dto.setMobileNumber(MOBILE);
        dto.setDateOfBirth(LocalDate.of(1990, 1, 1));
        dto.setStatus(status);
        return dto;
    }

}
