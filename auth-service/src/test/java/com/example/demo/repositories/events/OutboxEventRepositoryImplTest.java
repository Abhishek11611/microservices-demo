package com.example.demo.repositories.events;

import com.example.demo.entities.events.OutboxEvent;
import com.example.demo.enums.OutboxEventStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxEventRepositoryImplTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query query;

    @InjectMocks
    private OutboxEventRepositoryImpl repository;

    @Test
    void claimPendingEvents_marksClaimedEventsAsProcessingAndFlushes() {
        OutboxEvent first = event(1L);
        OutboxEvent second = event(2L);
        when(entityManager.createNativeQuery(anyString(), eq(OutboxEvent.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(first, second));

        List<OutboxEvent> result = repository.claimPendingEvents(100, 10);

        assertThat(result).containsExactly(first, second);
        assertThat(first.getStatus()).isEqualTo(OutboxEventStatus.PROCESSING);
        assertThat(second.getStatus()).isEqualTo(OutboxEventStatus.PROCESSING);
        verify(query).setParameter("maxRetryCount", 10);
        verify(query).setParameter("batchSize", 100);
        verify(entityManager).flush();
    }

    @Test
    void claimPendingEvents_withNoEvents_stillFlushes() {
        when(entityManager.createNativeQuery(anyString(), eq(OutboxEvent.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of());

        List<OutboxEvent> result = repository.claimPendingEvents(100, 10);

        assertThat(result).isEmpty();
        verify(entityManager).flush();
    }

    @Test
    void claimPendingEvents_whenQueryFails_propagatesExceptionAndDoesNotFlush() {
        when(entityManager.createNativeQuery(anyString(), eq(OutboxEvent.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenThrow(new RuntimeException("query failed"));

        assertThatThrownBy(() -> repository.claimPendingEvents(100, 10))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("query failed");

        verify(entityManager, org.mockito.Mockito.never()).flush();
    }

    @Test
    void claimPendingEvents_withInvalidBatchArguments_stillUsesProvidedValues() {
        when(entityManager.createNativeQuery(anyString(), eq(OutboxEvent.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of());

        repository.claimPendingEvents(0, -1);

        verify(query).setParameter("maxRetryCount", -1);
        verify(query).setParameter("batchSize", 0);
    }

    private OutboxEvent event(Long id) {
        OutboxEvent event = new OutboxEvent();
        event.setId(id);
        event.setStatus(OutboxEventStatus.PENDING);
        return event;
    }
}
