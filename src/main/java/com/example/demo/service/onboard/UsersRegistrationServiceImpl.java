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

    public UsersRegistrationServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public RegistrationResponse registrationPersonalDetails(RegistrationPersonalDetailsRequest registrationRequest) {
        String journeyID = UUID.randomUUID().toString();

        RegistrationDTO registrationDTO = new RegistrationDTO();
        registrationDTO.setFirstName(registrationRequest.firstName());
        registrationDTO.setEmail(registrationRequest.email());
        registrationDTO.setLastName(registrationRequest.lastName());
        registrationDTO.setUserCode(journeyID);
        registrationDTO.setDateOfBirth(registrationRequest.dateOfBirth());
        registrationDTO.setStatus(RegistrationStatus.PERSONAL_INFORMATION);
        registrationDTO.setJourneyId(journeyID);
        redisTemplate.opsForValue().set(journeyID,registrationDTO, Duration.ofMinutes(10));
        return new RegistrationResponse(journeyID,RegistrationStatus.PERSONAL_INFORMATION);
    }
}
