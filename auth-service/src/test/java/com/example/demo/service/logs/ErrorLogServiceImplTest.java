package com.example.demo.service.logs;

import com.example.demo.entities.logs.ErrorLog;
import com.example.demo.enums.AggregateType;
import com.example.demo.repositories.logs.ErrorLogRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ErrorLogServiceImplTest {

    @Mock
    private ErrorLogRepository errorLogRepository;

    @InjectMocks
    private ErrorLogServiceImpl errorLogService;

    @Test
    void saveError_persistsAllErrorDetails() {
        JsonNode payload = new ObjectMapper().createObjectNode().put("email", "john@example.com");
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);

        errorLogService.saveError(
                "journey-1",
                AggregateType.USER,
                payload,
                "VALIDATION_ERROR",
                "Invalid request",
                "stack trace"
        );

        ArgumentCaptor<ErrorLog> errorLogCaptor = ArgumentCaptor.forClass(ErrorLog.class);
        verify(errorLogRepository).save(errorLogCaptor.capture());
        ErrorLog savedLog = errorLogCaptor.getValue();
        assertThat(savedLog.getJourneyId()).isEqualTo("journey-1");
        assertThat(savedLog.getAggregateType()).isEqualTo(AggregateType.USER);
        assertThat(savedLog.getRequestPayload()).isEqualTo(payload);
        assertThat(savedLog.getErrorType()).isEqualTo("VALIDATION_ERROR");
        assertThat(savedLog.getErrorMessage()).isEqualTo("Invalid request");
        assertThat(savedLog.getStackTrace()).isEqualTo("stack trace");
        assertThat(savedLog.getCreatedAt()).isAfter(before);
    }

    @Test
    void saveError_withNullOptionalValues_stillPersistsLog() {
        errorLogService.saveError(null, null, null, null, null, null);

        ArgumentCaptor<ErrorLog> errorLogCaptor = ArgumentCaptor.forClass(ErrorLog.class);
        verify(errorLogRepository).save(errorLogCaptor.capture());
        ErrorLog savedLog = errorLogCaptor.getValue();
        assertThat(savedLog.getJourneyId()).isNull();
        assertThat(savedLog.getAggregateType()).isNull();
        assertThat(savedLog.getRequestPayload()).isNull();
        assertThat(savedLog.getErrorType()).isNull();
        assertThat(savedLog.getErrorMessage()).isNull();
        assertThat(savedLog.getStackTrace()).isNull();
        assertThat(savedLog.getCreatedAt()).isNotNull();
    }

    @Test
    void saveError_whenRepositoryFails_propagatesException() {
        when(errorLogRepository.save(any(ErrorLog.class))).thenThrow(new RuntimeException("database down"));

        assertThatThrownBy(() -> errorLogService.saveError(
                "journey-1", AggregateType.USER, null, "ERROR", "message", "stack"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("database down");
    }
}
