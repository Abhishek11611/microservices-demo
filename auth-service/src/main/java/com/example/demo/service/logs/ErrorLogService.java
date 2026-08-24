package com.example.demo.service.logs;

public interface ErrorLogService {

    void saveError(String journeyId, String errorType, String errorMessage, String stackTrace);
}
