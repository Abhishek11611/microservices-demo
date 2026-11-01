package com.example.demo.service.onboard;

import com.example.demo.dtos.events.UserRegisteredEvent;
import com.example.demo.dtos.registration.RegistrationDTO;
import com.example.demo.entities.users.AuthUser;
import com.example.demo.service.outbox.OutboxEventService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRegistrationPersistenceServiceImplTest {

    @Mock
    private AuthUserService authUserService;

    @Mock
    private UserRegisteredEventFactory eventFactory;

    @Mock
    private OutboxEventService outboxEventService;

    @InjectMocks
    private UserRegistrationPersistenceServiceImpl persistenceService;

    @Test
    void register_createsUserEventAndOutboxEntryInOrder() {
        RegistrationDTO dto = registrationDto();
        AuthUser user = new AuthUser();
        UserRegisteredEvent event = new UserRegisteredEvent(
                "UABC123", "John", "Doe", "john@example.com", "9999999999",
                LocalDate.of(1990, 1, 1), List.of("USER")
        );
        when(authUserService.createUser(dto)).thenReturn(user);
        when(eventFactory.create(user)).thenReturn(event);

        persistenceService.register(dto);

        InOrder inOrder = inOrder(authUserService, eventFactory, outboxEventService);
        inOrder.verify(authUserService).createUser(dto);
        inOrder.verify(eventFactory).create(user);
        inOrder.verify(outboxEventService).save(event, user);
    }

    @Test
    void register_whenUserCreationFails_doesNotCreateEventOrOutboxEntry() {
        RegistrationDTO dto = registrationDto();
        when(authUserService.createUser(dto)).thenThrow(new RuntimeException("user creation failed"));

        assertThatThrownBy(() -> persistenceService.register(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("user creation failed");

        verifyNoInteractions(eventFactory, outboxEventService);
    }

    @Test
    void register_whenEventCreationFails_doesNotSaveOutboxEntry() {
        RegistrationDTO dto = registrationDto();
        AuthUser user = new AuthUser();
        when(authUserService.createUser(dto)).thenReturn(user);
        when(eventFactory.create(user)).thenThrow(new RuntimeException("event creation failed"));

        assertThatThrownBy(() -> persistenceService.register(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("event creation failed");

        verifyNoInteractions(outboxEventService);
    }

    @Test
    void register_whenOutboxSaveFails_propagatesException() {
        RegistrationDTO dto = registrationDto();
        AuthUser user = new AuthUser();
        UserRegisteredEvent event = new UserRegisteredEvent(
                "UABC123", "John", "Doe", "john@example.com", "9999999999",
                LocalDate.of(1990, 1, 1), List.of("USER")
        );
        when(authUserService.createUser(dto)).thenReturn(user);
        when(eventFactory.create(user)).thenReturn(event);
        org.mockito.Mockito.doThrow(new RuntimeException("outbox failed"))
                .when(outboxEventService).save(event, user);

        assertThatThrownBy(() -> persistenceService.register(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("outbox failed");
    }

    private RegistrationDTO registrationDto() {
        RegistrationDTO dto = new RegistrationDTO();
        dto.setUserCode("UABC123");
        dto.setEmail("john@example.com");
        dto.setMobileNumber("9999999999");
        dto.setDateOfBirth(LocalDate.of(1990, 1, 1));
        return dto;
    }
}
