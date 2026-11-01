package com.example.demo.service.outbox;

import com.example.demo.entities.events.OutboxEvent;
import com.example.demo.enums.AggregateType;
import com.example.demo.enums.EventType;
import com.example.demo.enums.OutboxEventStatus;
import com.example.demo.repositories.events.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.SendResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxEventPublisherTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private KafkaEventProducer kafkaEventProducer;

    private ObjectMapper objectMapper;

    private OutboxEventPublisher publisher;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        publisher = new OutboxEventPublisher(outboxEventRepository, kafkaEventProducer, objectMapper);
    }

    @Test
    void publishPendingEvents_withNoPendingEvents_doesNothing() {
        when(outboxEventRepository.claimPendingEvents(100, 10)).thenReturn(List.of());

        publisher.publishPendingEvents();

        verifyNoInteractions(kafkaEventProducer);
        verify(outboxEventRepository, never()).save(any(OutboxEvent.class));
    }

    @Test
    void publishPendingEvents_whenPublishSucceeds_marksEventPublished() {
        OutboxEvent event = event(1L, "UABC123");
        when(outboxEventRepository.claimPendingEvents(100, 10)).thenReturn(List.of(event));
        when(kafkaEventProducer.publish(eq("user-registration"), eq("UABC123"), anyString()))
                .thenReturn(successfulPublish());

        publisher.publishPendingEvents();

        assertThat(event.getStatus()).isEqualTo(OutboxEventStatus.PUBLISHED);
        assertThat(event.getPublishedAt()).isNotNull();
        assertThat(event.getRetryCount()).isZero();
        verify(outboxEventRepository).save(event);

        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaEventProducer).publish(eq("user-registration"), eq("UABC123"), payloadCaptor.capture());
        assertThat(payloadCaptor.getValue())
                .contains(event.getEventId().toString())
                .contains("USER_REGISTERED")
                .contains("UABC123");
    }

    @Test
    void publishPendingEvents_whenPublishFails_marksEventFailedAndIncrementsRetryCount() {
        OutboxEvent event = event(1L, "UABC123");
        when(outboxEventRepository.claimPendingEvents(100, 10)).thenReturn(List.of(event));
        when(kafkaEventProducer.publish(eq("user-registration"), eq("UABC123"), anyString()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("kafka down")));

        publisher.publishPendingEvents();

        assertThat(event.getStatus()).isEqualTo(OutboxEventStatus.FAILED);
        assertThat(event.getRetryCount()).isEqualTo(1);
        assertThat(event.getPublishedAt()).isNull();
        verify(outboxEventRepository).save(event);
    }

    @Test
    void publishPendingEvents_whenPublishTimesOut_marksEventFailed() {
        OutboxEvent event = event(1L, "UABC123");
        when(outboxEventRepository.claimPendingEvents(100, 10)).thenReturn(List.of(event));
        when(kafkaEventProducer.publish(eq("user-registration"), eq("UABC123"), anyString()))
                .thenReturn(CompletableFuture.failedFuture(new TimeoutException("timed out")));

        publisher.publishPendingEvents();

        assertThat(event.getStatus()).isEqualTo(OutboxEventStatus.FAILED);
        assertThat(event.getRetryCount()).isEqualTo(1);
        verify(outboxEventRepository).save(event);
    }

    @Test
    void publishPendingEvents_whenSerializationFails_marksEventFailed() throws Exception {
        ObjectMapper failingObjectMapper = mock(ObjectMapper.class);
        when(failingObjectMapper.writeValueAsString(any()))
                .thenThrow(new JsonProcessingException("serialization failed") {
                });
        OutboxEventPublisher failingPublisher =
                new OutboxEventPublisher(outboxEventRepository, kafkaEventProducer, failingObjectMapper);
        OutboxEvent event = event(1L, "UABC123");
        when(outboxEventRepository.claimPendingEvents(100, 10)).thenReturn(List.of(event));

        failingPublisher.publishPendingEvents();

        assertThat(event.getStatus()).isEqualTo(OutboxEventStatus.FAILED);
        assertThat(event.getRetryCount()).isEqualTo(1);
        verify(kafkaEventProducer, never()).publish(anyString(), anyString(), anyString());
        verify(outboxEventRepository).save(event);
    }

    @Test
    void publishPendingEvents_continuesProcessingAfterOneEventFails() {
        OutboxEvent failingEvent = event(1L, "UFAIL");
        OutboxEvent successfulEvent = event(2L, "UOK");
        when(outboxEventRepository.claimPendingEvents(100, 10))
                .thenReturn(List.of(failingEvent, successfulEvent));
        when(kafkaEventProducer.publish(eq("user-registration"), eq("UFAIL"), anyString()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("kafka down")));
        when(kafkaEventProducer.publish(eq("user-registration"), eq("UOK"), anyString()))
                .thenReturn(successfulPublish());

        publisher.publishPendingEvents();

        assertThat(failingEvent.getStatus()).isEqualTo(OutboxEventStatus.FAILED);
        assertThat(successfulEvent.getStatus()).isEqualTo(OutboxEventStatus.PUBLISHED);
        verify(outboxEventRepository, times(2)).save(any(OutboxEvent.class));
    }

    @Test
    void publishPendingEvents_whenClaimingEventsFails_propagatesException() {
        when(outboxEventRepository.claimPendingEvents(100, 10))
                .thenThrow(new RuntimeException("database down"));

        assertThatThrownBy(() -> publisher.publishPendingEvents())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("database down");

        verifyNoInteractions(kafkaEventProducer);
    }

    private OutboxEvent event(Long id, String eventKey) {
        OutboxEvent event = new OutboxEvent();
        event.setId(id);
        event.setEventId(UUID.randomUUID());
        event.setAggregateType(AggregateType.USER);
        event.setAggregateId(10L);
        event.setEventType(EventType.USER_REGISTERED);
        event.setEventKey(eventKey);
        event.setPayload(objectMapper.createObjectNode().put("userCode", eventKey));
        event.setCreatedAt(LocalDateTime.now());
        event.setRetryCount(0);
        event.setStatus(OutboxEventStatus.PENDING);
        return event;
    }

    @SuppressWarnings("unchecked")
    private CompletableFuture<SendResult<String, String>> successfulPublish() {
        SendResult<String, String> sendResult = mock(SendResult.class);
        return CompletableFuture.completedFuture(sendResult);
    }
}
