package com.example.demo.service.logs;

import com.example.demo.entities.logs.ErrorLog;
import com.example.demo.enums.AggregateType;
import com.example.demo.repositories.logs.ErrorLogRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ErrorLogServiceImpl implements ErrorLogService {

    private final ErrorLogRepository errorLogRepository;

    public ErrorLogServiceImpl(ErrorLogRepository errorLogRepository) {
        this.errorLogRepository = errorLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void saveError(String journeyId, AggregateType aggregateType, JsonNode requestPayLoad, String errorType, String errorMessage, String stackTrace) {

        ErrorLog errorLog = new ErrorLog();

        errorLog.setJourneyId(journeyId);
        errorLog.setAggregateType(aggregateType);
        errorLog.setRequestPayload(requestPayLoad);
        errorLog.setErrorType(errorType);
        errorLog.setErrorMessage(errorMessage);
        errorLog.setStackTrace(stackTrace);
        errorLog.setCreatedAt(LocalDateTime.now());

        errorLogRepository.save(errorLog);

    }
}
