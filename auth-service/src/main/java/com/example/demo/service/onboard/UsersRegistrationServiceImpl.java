package com.example.demo.service.onboard;

import com.example.commoncore.exception.BadRequestException;
import com.example.commoncore.exception.NotFoundException;
import com.example.demo.dtos.registration.*;
import com.example.demo.enums.RegistrationStatus;
import com.example.demo.repositories.UsersRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class UsersRegistrationServiceImpl implements UsersRegistrationService{

    private static final Duration REGISTRATION_TTL = Duration.ofMinutes(10);
    private static final String REGISTRATION_KEY_PREFIX = "registration:";

    private final RedisTemplate<String,Object> redisTemplate;
    private final OtpService otpService;
    private final ObjectMapper objectMapper;
    private final PasswordEncoder passwordEncoder;
    private final UsersRepository usersRepository;

    public UsersRegistrationServiceImpl(RedisTemplate<String, Object> redisTemplate, OtpService otpService, ObjectMapper objectMapper, PasswordEncoder passwordEncoder, UsersRepository usersRepository) {
        this.redisTemplate = redisTemplate;
        this.otpService = otpService;
        this.objectMapper = objectMapper;
        this.passwordEncoder = passwordEncoder;
        this.usersRepository = usersRepository;
    }

    @Override
    public RegistrationResponse registrationPersonalDetails(RegistrationPersonalDetailsRequest registrationRequest) {

        String journeyID = UUID.randomUUID().toString();
        String userCode = "U" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

       if(usersRepository.existsByUserCode(userCode)){
           throw new BadRequestException("UserCode already exists Please Try Again.");
       }

        if(usersRepository.existsByEmail(registrationRequest.email())){
            throw new BadRequestException("Email already exists.");
        }

        if(usersRepository.existsByMobileNumber(registrationRequest.mobileNumber())){
            throw new BadRequestException("Mobile Number already exists.");
        }

        String emailKey = "email:" + registrationRequest.email();
        Boolean emailSuccess = redisTemplate.opsForValue().setIfAbsent(emailKey, journeyID,REGISTRATION_TTL );

        String mobileNumberKey = "mobileNumber:" + registrationRequest.mobileNumber();
        Boolean mobileSuccess = redisTemplate.opsForValue().setIfAbsent(mobileNumberKey, journeyID, REGISTRATION_TTL);

        if (!Boolean.TRUE.equals(emailSuccess)){
            throw new BadRequestException("Email is already being used in another registration.");
        }

        if (!Boolean.TRUE.equals(mobileSuccess)){
            throw new BadRequestException("Mobile Number is already being used in another registration.");
        }

        RegistrationDTO registrationDTO = new RegistrationDTO();
        registrationDTO.setUserCode(userCode);
        registrationDTO.setFirstName(registrationRequest.firstName());
        registrationDTO.setLastName(registrationRequest.lastName());
        registrationDTO.setEmail(registrationRequest.email());
        registrationDTO.setDateOfBirth(registrationRequest.dateOfBirth());
        registrationDTO.setMobileNumber(registrationRequest.mobileNumber());
        registrationDTO.setStatus(RegistrationStatus.PERSONAL_INFORMATION);
        registrationDTO.setJourneyId(journeyID);

        redisTemplate.opsForValue().set(REGISTRATION_KEY_PREFIX+journeyID,registrationDTO, REGISTRATION_TTL);

        String generatedOTP = otpService.generateAndStore(journeyID);

        return new RegistrationResponse(journeyID,RegistrationStatus.PERSONAL_INFORMATION);
    }

    @Override
    public RegistrationResponse registrationVerifyOtp(RegistrationOTPRequest registrationOTPRequest) {

        Object redisObject = redisTemplate.opsForValue().get(REGISTRATION_KEY_PREFIX+registrationOTPRequest.journeyId());

        if (redisObject == null) {
            throw new NotFoundException("Registration session is invalid or has expired");
        }

        RegistrationDTO registrationDTO = objectMapper.convertValue(redisObject, RegistrationDTO.class);

        Long ttlExpire = redisTemplate.getExpire(REGISTRATION_KEY_PREFIX+registrationDTO.getJourneyId(), TimeUnit.SECONDS);

        if (!RegistrationStatus.PERSONAL_INFORMATION.equals(registrationDTO.getStatus())){
            throw new BadRequestException("Please complete your personal information before verifying your OTP.");
        }

        Boolean isOTPVerified = otpService.verifyOtp(registrationOTPRequest.journeyId(), registrationOTPRequest.otp());

        if (!Boolean.TRUE.equals(isOTPVerified)) {
            throw new BadRequestException("The OTP you entered is invalid or has expired.");
        }

        registrationDTO.setStatus(RegistrationStatus.OTP_VERIFIED);

        redisTemplate.opsForValue().set(REGISTRATION_KEY_PREFIX+registrationDTO.getJourneyId(),registrationDTO,ttlExpire,TimeUnit.SECONDS);

        return new  RegistrationResponse(registrationDTO.getJourneyId(),RegistrationStatus.OTP_VERIFIED);
    }

    @Override
    public RegistrationResponse registrationPassword(PasswordRequest passwordRequest) {

        if (!passwordRequest.password().equals(passwordRequest.confirmPassword())){
            throw new BadRequestException("Passwords do not match");
        }

        Object redisObject = redisTemplate.opsForValue().get(REGISTRATION_KEY_PREFIX+passwordRequest.journeyId());

        if (redisObject == null) {
            throw new NotFoundException("Registration session is invalid or has expired.");
        }

        RegistrationDTO registrationDTO = objectMapper.convertValue(redisObject, RegistrationDTO.class);

        if (registrationDTO.getStatus().equals(RegistrationStatus.PASSWORD_SET)){
            throw new BadRequestException("Password has already been set.");
        }

            if (!RegistrationStatus.OTP_VERIFIED.equals(registrationDTO.getStatus())){
            throw new BadRequestException("Please complete your Verify OTP before Set password.");
        }
        String encodePassword = passwordEncoder.encode(passwordRequest.password());
        registrationDTO.setPasswordHash(encodePassword);
        registrationDTO.setStatus(RegistrationStatus.PASSWORD_SET);

        Long ttlExpire = redisTemplate.getExpire(REGISTRATION_KEY_PREFIX+registrationDTO.getJourneyId(), TimeUnit.SECONDS);

        redisTemplate.opsForValue().set(REGISTRATION_KEY_PREFIX+passwordRequest.journeyId(),registrationDTO,ttlExpire,TimeUnit.SECONDS);

        return new RegistrationResponse(registrationDTO.getJourneyId(),RegistrationStatus.PASSWORD_SET);
    }


}
