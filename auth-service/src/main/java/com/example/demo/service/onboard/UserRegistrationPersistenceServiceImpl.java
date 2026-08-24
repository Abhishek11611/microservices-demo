package com.example.demo.service.onboard;

import com.example.demo.dtos.events.UserRegisteredEvent;
import com.example.demo.dtos.registration.RegistrationDTO;
import com.example.demo.entities.events.OutboxEvent;
import com.example.demo.entities.users.AuthUser;
import com.example.demo.enums.AggregateType;
import com.example.demo.enums.EventType;
import com.example.demo.enums.OutboxEventStatus;
import com.example.demo.repositories.AuthUsersRepository;
import com.example.demo.repositories.events.OutboxEventRepository;
import com.example.demo.service.logs.ErrorLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;

@Service
public class UserRegistrationPersistenceServiceImpl implements UserRegistrationPersistenceService{

    private final AuthUsersRepository authUsersRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ErrorLogService errorLogService;
    private final ObjectMapper objectMapper;

    public UserRegistrationPersistenceServiceImpl(AuthUsersRepository authUsersRepository, OutboxEventRepository outboxEventRepository, ErrorLogService errorLogService, ObjectMapper objectMapper) {
        this.authUsersRepository = authUsersRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.errorLogService = errorLogService;
        this.objectMapper = objectMapper;
    }


    @Transactional
    @Override
    public void saveAuthUserWithEvent(RegistrationDTO registrationDTO) {

    try {

        AuthUser authUser = new AuthUser();
        authUser.setUserCode(registrationDTO.getUserCode());
        authUser.setFirstName(registrationDTO.getFirstName());
        authUser.setLastName(registrationDTO.getLastName());
        authUser.setDateOfBirth(registrationDTO.getDateOfBirth());
        authUser.setEmail(registrationDTO.getEmail());
        authUser.setMobileNumber(registrationDTO.getMobileNumber());
        authUser.setPasswordHash(registrationDTO.getPasswordHash());

        AuthUser user = authUsersRepository.save(authUser);

        UserRegisteredEvent userRegisteredEvent =
                new UserRegisteredEvent(user.getUserCode(), user.getFirstName(),
                        user.getLastName(), user.getEmail(), user.getMobileNumber(),
                        user.getDateOfBirth());

        OutboxEvent outboxEvent = new OutboxEvent();

        outboxEvent.setEventId(UUID.randomUUID());
        outboxEvent.setAggregateType(AggregateType.USER);
        outboxEvent.setAggregateId(user.getId());
        outboxEvent.setEventType(EventType.USER_REGISTERED);
        outboxEvent.setEventKey(user.getUserCode());
        outboxEvent.setPayload(objectMapper.valueToTree(userRegisteredEvent));
        outboxEvent.setPublishedAt(null);
        outboxEvent.setRetryCount(0);
        outboxEvent.setStatus(OutboxEventStatus.PENDING);

        outboxEventRepository.save(outboxEvent);

    }catch (Exception e){
        errorLogService.saveError( registrationDTO.getJourneyId(),
                e.getClass().getSimpleName(),
                e.getMessage(),
                getStackTrace(e));
    }
    }
}
