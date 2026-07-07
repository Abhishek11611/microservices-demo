package com.example.demo.service.onboard;

import com.example.demo.dtos.registration.RegistrationDTO;
import com.example.demo.dtos.registration.RegistrationPersonalDetailsRequest;
import com.example.demo.dtos.registration.RegistrationResponse;
import com.example.demo.enums.RegistrationStatus;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;
@Service
public class UsersRegistrationServiceImpl implements UsersRegistrationService{

    private final RedisTemplate<String,Object> redisTemplate;
    private final OtpService otpService;

    public UsersRegistrationServiceImpl(RedisTemplate<String, Object> redisTemplate, OtpService otpService) {
        this.redisTemplate = redisTemplate;
        this.otpService = otpService;
    }

    @Override
    public RegistrationResponse registrationPersonalDetails(RegistrationPersonalDetailsRequest registrationRequest) {

        String journeyID = UUID.randomUUID().toString();
        String userCode = "U" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        RegistrationDTO registrationDTO = new RegistrationDTO();
        registrationDTO.setUserCode(userCode);
        registrationDTO.setFirstName(registrationRequest.firstName());
        registrationDTO.setLastName(registrationRequest.lastName());
        registrationDTO.setEmail(registrationRequest.email());
        registrationDTO.setDateOfBirth(registrationRequest.dateOfBirth());
        registrationDTO.setMobileNumber(registrationRequest.mobileNumber());
        registrationDTO.setStatus(RegistrationStatus.PERSONAL_INFORMATION);
        registrationDTO.setJourneyId(journeyID);

        redisTemplate.opsForValue().set(journeyID,registrationDTO, Duration.ofMinutes(10));

        return new RegistrationResponse(journeyID,RegistrationStatus.PERSONAL_INFORMATION);
    }

    @Override
    public RegistrationResponse registrationVerifyOtp(RegistrationPersonalDetailsRequest registrationPersonalDetailsRequest) {

        return null;
    }


}
