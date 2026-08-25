package com.example.demo.service.outbox;

import com.example.demo.dtos.events.UserRegisteredEvent;
import com.example.demo.entities.events.OutboxEvent;
import com.example.demo.entities.users.AuthUser;
import com.example.demo.enums.AggregateType;
import com.example.demo.enums.EventType;
import com.example.demo.enums.OutboxEventStatus;
import com.example.demo.repositories.events.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public OutboxEventService(OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    public void save(UserRegisteredEvent event,AuthUser user) {

        OutboxEvent outboxEvent = new OutboxEvent();

        outboxEvent.setEventId(UUID.randomUUID());
        outboxEvent.setAggregateType(AggregateType.USER);
        outboxEvent.setAggregateId(user.getId());
        outboxEvent.setEventType(EventType.USER_REGISTERED);
        outboxEvent.setEventKey(user.getUserCode());
        outboxEvent.setPayload(
                objectMapper.valueToTree(event)
        );
        outboxEvent.setCreatedAt(LocalDateTime.now());
        outboxEvent.setRetryCount(0);
        outboxEvent.setStatus(
                OutboxEventStatus.PENDING
        );

        outboxEventRepository.save(outboxEvent);
    }
}
