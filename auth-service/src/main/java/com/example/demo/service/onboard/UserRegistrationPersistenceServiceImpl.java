package com.example.demo.service.onboard;


import com.example.demo.dtos.events.UserRegisteredEvent;
import com.example.demo.dtos.registration.RegistrationDTO;
import com.example.demo.entities.users.AuthUser;

import com.example.demo.service.outbox.OutboxEventService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;


@Service
public class UserRegistrationPersistenceServiceImpl implements UserRegistrationPersistenceService{

    private final AuthUserService authUserService;
    private final UserRegisteredEventFactory eventFactory;
    private final OutboxEventService outboxEventService;

    public UserRegistrationPersistenceServiceImpl(AuthUserService authUserService, UserRegisteredEventFactory eventFactory, OutboxEventService outboxEventService) {
        this.authUserService = authUserService;
        this.eventFactory = eventFactory;
        this.outboxEventService = outboxEventService;
    }

    @Transactional
    public void register(RegistrationDTO dto) {

        AuthUser user = authUserService.createUser(dto);

        UserRegisteredEvent event = eventFactory.create(user);

        outboxEventService.save(event, user);
    }
}
