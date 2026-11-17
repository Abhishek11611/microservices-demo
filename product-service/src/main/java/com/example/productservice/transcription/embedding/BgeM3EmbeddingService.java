package com.example.productservice.transcription.embedding;

import java.util.Map;

public interface BgeM3EmbeddingService {

    Map<String, Object> generateEmbedding(String text);

    float[] generateVector(String text);
}