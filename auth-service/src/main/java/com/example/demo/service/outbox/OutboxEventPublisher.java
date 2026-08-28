package com.example.demo.service.outbox;

import com.example.demo.dtos.events.EventEnvelope;
import com.example.demo.entities.events.OutboxEvent;
import com.example.demo.enums.OutboxEventStatus;
import com.example.demo.repositories.events.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class OutboxEventPublisher {

    private static final int BATCH_SIZE = 100;
    private static final int MAX_RETRY_COUNT = 10;

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaEventProducer kafkaEventProducer;
    private final ObjectMapper objectMapper;

    public OutboxEventPublisher(OutboxEventRepository outboxEventRepository, KafkaEventProducer kafkaEventProducer, ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaEventProducer = kafkaEventProducer;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 5000)
    public void publishPendingEvents() {

        List<OutboxEvent> pendingEvents = outboxEventRepository.
                claimPendingEvents(BATCH_SIZE, MAX_RETRY_COUNT);


                for (OutboxEvent event : pendingEvents) {

                    try {

                        EventEnvelope eventEnvelope = new EventEnvelope(
                                event.getEventId(),
                                event.getEventType().name(),
                                event.getEventKey(),
                                event.getPayload()
                        );

                        kafkaEventProducer.publish(
                                "user-registration",
                                event.getEventKey(),
                                objectMapper.writeValueAsString(eventEnvelope)
                        ).get(10, TimeUnit.SECONDS);


                        event.setStatus(OutboxEventStatus.PUBLISHED);
                        event.setPublishedAt(LocalDateTime.now());

                        outboxEventRepository.save(event);
                    } catch (Exception ex) {

                        event.setRetryCount(event.getRetryCount() + 1);
                        event.setStatus(OutboxEventStatus.FAILED);
                        outboxEventRepository.save(event);

                    }
                }
            }


}