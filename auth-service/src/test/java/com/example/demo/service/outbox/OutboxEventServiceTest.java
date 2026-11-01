package com.example.demo.service.outbox;

import com.example.demo.dtos.events.UserRegisteredEvent;
import com.example.demo.entities.events.OutboxEvent;
import com.example.demo.entities.users.AuthUser;
import com.example.demo.enums.AggregateType;
import com.example.demo.enums.EventType;
import com.example.demo.enums.OutboxEventStatus;
import com.example.demo.repositories.events.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxEventServiceTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    private OutboxEventService outboxEventService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        outboxEventService = new OutboxEventService(outboxEventRepository, objectMapper);
    }

    @Test
    void save_persistsOutboxEventWithUserAndEventData() {
        UserRegisteredEvent event = registeredEvent();
        AuthUser user = user();

        outboxEventService.save(event, user);

        ArgumentCaptor<OutboxEvent> eventCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(eventCaptor.capture());
        OutboxEvent savedEvent = eventCaptor.getValue();
        assertThat(savedEvent.getEventId()).isNotNull();
        assertThat(savedEvent.getAggregateType()).isEqualTo(AggregateType.USER);
        assertThat(savedEvent.getAggregateId()).isEqualTo(10L);
        assertThat(savedEvent.getEventType()).isEqualTo(EventType.USER_REGISTERED);
        assertThat(savedEvent.getEventKey()).isEqualTo("UABC123");
        assertThat(savedEvent.getPayload()).isEqualTo(objectMapper.valueToTree(event));
        assertThat(savedEvent.getCreatedAt()).isNotNull();
        assertThat(savedEvent.getRetryCount()).isZero();
        assertThat(savedEvent.getStatus()).isEqualTo(OutboxEventStatus.PENDING);
    }

    @Test
    void save_whenRepositoryFails_propagatesException() {
        UserRegisteredEvent event = registeredEvent();
        AuthUser user = user();
        when(outboxEventRepository.save(any(OutboxEvent.class))).thenThrow(new RuntimeException("database down"));

        assertThatThrownBy(() -> outboxEventService.save(event, user))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("database down");
    }

    @Test
    void save_withNullUser_throwsNullPointerException() {
        assertThatThrownBy(() -> outboxEventService.save(registeredEvent(), null))
                .isInstanceOf(NullPointerException.class);
    }

    private UserRegisteredEvent registeredEvent() {
        return new UserRegisteredEvent(
                "UABC123",
                "John",
                "Doe",
                "john@example.com",
                "9999999999",
                LocalDate.of(1990, 1, 1),
                List.of("USER")
        );
    }

    private AuthUser user() {
        AuthUser user = new AuthUser();
        user.setId(10L);
        user.setUserCode("UABC123");
        return user;
    }
}
