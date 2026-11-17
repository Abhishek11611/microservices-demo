
package com.example.productservice.transcription.service;

import java.util.Map;

public interface VideoQuestionAnswerService {

    Map<String, Object> askQuestion(
            String question,
            int limit
    );
}
