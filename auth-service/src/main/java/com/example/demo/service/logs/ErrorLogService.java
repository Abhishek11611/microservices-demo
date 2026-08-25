package com.example.demo.service.logs;

import com.example.demo.enums.AggregateType;
import com.fasterxml.jackson.databind.JsonNode;

public interface ErrorLogService {

    void saveError(String journeyId, AggregateType aggregateType, JsonNode requestPayLoad, String errorType, String errorMessage, String stackTrace);
}
