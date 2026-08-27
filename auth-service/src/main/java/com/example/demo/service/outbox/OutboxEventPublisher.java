package com.example.demo.service.outbox;

import com.example.demo.dtos.events.EventEnvelope;
import com.example.demo.entities.events.OutboxEvent;
import com.example.demo.enums.OutboxEventStatus;
import com.example.demo.repositories.events.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class OutboxEventPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaEventProducer kafkaEventProducer;
    private final ObjectMapper objectMapper;

    public OutboxEventPublisher(OutboxEventRepository outboxEventRepository, KafkaEventProducer kafkaEventProducer, ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaEventProducer = kafkaEventProducer;
        this.objectMapper = objectMapper;
    }


    public void publishPendingEvents() {

        List<OutboxEvent> pendingEvents = outboxEventRepository.
                findByStatusInAndPublishedAtIsNullAndRetryCountLessThanOrderByIdAsc(
                        List.of(OutboxEventStatus.PENDING, OutboxEventStatus.FAILED), 10);


        pendingEvents.forEach(event -> {
                    try {
                        event.setRetryCount(event.getRetryCount() + 1);

                        EventEnvelope eventEnvelope = new EventEnvelope(
                                event.getEventId(),
                                event.getEventType().name(),
                                event.getEventKey(),
                                event.getPayload()
                        );

                        CompletableFuture<SendResult<String, String>> future = kafkaEventProducer.publish(
                                "user-registration",
                                event.getEventKey(),
                                objectMapper.writeValueAsString(eventEnvelope)
                        );

                        future.whenComplete((result, exception) -> {

                            if (exception == null) {

                                // Kafka acknowledgement received
                                event.setStatus(OutboxEventStatus.PUBLISHED);
                                event.setPublishedAt(LocalDateTime.now());

                            } else {

                                // Kafka publish failed
                                event.setStatus(OutboxEventStatus.FAILED);

                                // log exception
                            }

                        });

                        event.setStatus(OutboxEventStatus.PUBLISHED);
                        event.setPublishedAt(LocalDateTime.now());
                    } catch (Exception e) {
                        event.setStatus(OutboxEventStatus.FAILED);
                    } finally {
                        outboxEventRepository.save(event);
                    }
                }
        );
    }


}