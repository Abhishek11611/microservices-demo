
package com.example.productservice.transcription.controller;

import com.example.productservice.transcription.embedding.BgeM3EmbeddingService;
import com.example.productservice.transcription.vector.QdrantStorageService;

import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/audio/qdrant")
public class QdrantTestController {

    private final BgeM3EmbeddingService embeddingService;
    private final QdrantStorageService storageService;

    public QdrantTestController(
            BgeM3EmbeddingService embeddingService,
            QdrantStorageService storageService) {

        this.embeddingService = embeddingService;
        this.storageService = storageService;
    }

    @PostMapping("/test-save")
    public Map<String, Object> testSave() {

        String text = "What is a collection?";

        float[] vector =
                embeddingService.generateVector(text);

        String pointId = UUID.randomUUID().toString();

        Map<String, Object> payload = new LinkedHashMap<>();

        payload.put("videoId", "video-101");
        payload.put("segment", 1);
        payload.put("startSeconds", 0.0);
        payload.put("endSeconds", 3.36);
        payload.put("duration", 3.36);
        payload.put("text", text);
        payload.put("model", "BAAI/bge-m3");

        storageService.saveEmbedding(
                pointId,
                vector,
                payload
        );

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("message", "Embedding saved successfully");
        response.put("pointId", pointId);
        response.put("dimensions", vector.length);
        response.put("text", text);

        return response;
    }
}
