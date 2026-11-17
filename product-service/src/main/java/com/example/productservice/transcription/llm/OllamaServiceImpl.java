
package com.example.productservice.transcription.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OllamaServiceImpl implements OllamaService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String model;
    private final int contextSize;
    private final double temperature;
    private final int maxOutputTokens;

    public OllamaServiceImpl(
            @Value("${ollama.base-url}") String baseUrl,
            @Value("${ollama.model}") String model,
            @Value("${ollama.context-size}") int contextSize,
            @Value("${ollama.temperature}") double temperature,
            @Value("${ollama.max-output-tokens}") int maxOutputTokens,
            ObjectMapper objectMapper) {

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();

        this.objectMapper = objectMapper;
        this.model = model;
        this.contextSize = contextSize;
        this.temperature = temperature;
        this.maxOutputTokens = maxOutputTokens;
    }

    @Override
    public String generateAnswer(String question, String context) {

        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException(
                    "Question cannot be empty"
            );
        }

        if (context == null || context.isBlank()) {
            throw new IllegalArgumentException(
                    "Transcript context cannot be empty"
            );
        }

        try {

            String prompt = """
                    You are a video transcript question-answering assistant.

                    Answer the user's question using ONLY the transcript context.

                    Rules:
                    1. Give a clear and complete answer in simple English.
                    2. Never return incomplete sentences.
                    3. Do not invent information.
                    4. If the answer is not available in the transcript,
                       clearly say that it was not found.
                    5. Do not include your thinking process.
                    6. Return only the final answer.

                    TRANSCRIPT CONTEXT:
                    %s

                    USER QUESTION:
                    %s
                    """.formatted(context, question);

            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);

            Map<String, Object> options = new HashMap<>();
            options.put("temperature", temperature);
            options.put("num_ctx", contextSize);
            options.put("num_predict", maxOutputTokens);

            Map<String, Object> request = new HashMap<>();
            request.put("model", model);
            request.put("messages", List.of(message));
            request.put("stream", false);
            request.put("think", false);
            request.put("options", options);

            String response = restClient.post()
                    .uri("/api/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(String.class);

            JsonNode json = objectMapper.readTree(response);

            String answer = json.path("message")
                    .path("content")
                    .asText();

            if (answer == null || answer.isBlank()) {
                throw new RuntimeException(
                        "Ollama returned an empty answer"
                );
            }

            return answer.trim();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Unable to generate answer using Ollama", e
            );
        }
    }
}
