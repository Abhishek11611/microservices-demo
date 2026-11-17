
package com.example.productservice.transcription.llm;

import java.util.Map;

public interface OllamaService {

    String generateAnswer(
            String question,
            String context
    );
}
