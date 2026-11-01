package com.example.demo.service.outbox;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaEventProducerTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private KafkaEventProducer kafkaEventProducer;

    @Test
    @SuppressWarnings("unchecked")
    void publish_delegatesToKafkaTemplateAndReturnsFuture() {
        SendResult<String, String> sendResult = mock(SendResult.class);
        CompletableFuture<SendResult<String, String>> future = CompletableFuture.completedFuture(sendResult);
        when(kafkaTemplate.send("user-registration", "UABC123", "payload")).thenReturn(future);

        CompletableFuture<SendResult<String, String>> result =
                kafkaEventProducer.publish("user-registration", "UABC123", "payload");

        assertThat(result).isSameAs(future);
        verify(kafkaTemplate).send("user-registration", "UABC123", "payload");
    }

    @Test
    void publish_whenKafkaTemplateThrows_propagatesException() {
        when(kafkaTemplate.send("topic", "key", "payload"))
                .thenThrow(new RuntimeException("kafka unavailable"));

        assertThatThrownBy(() -> kafkaEventProducer.publish("topic", "key", "payload"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("kafka unavailable");
    }
}
