
package com.example.productservice.transcription.controller;

import com.example.productservice.transcription.embedding.BgeM3EmbeddingService;
import com.example.productservice.transcription.vector.QdrantSearchService;

import io.qdrant.client.grpc.Points.ScoredPoint;
import io.qdrant.client.grpc.JsonWithInt.Value;

import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/video")
public class VideoSearchController {

    private final BgeM3EmbeddingService embeddingService;
    private final QdrantSearchService searchService;

    public VideoSearchController(
            BgeM3EmbeddingService embeddingService,
            QdrantSearchService searchService) {

        this.embeddingService = embeddingService;
        this.searchService = searchService;
    }

    @GetMapping("/search")
    public Map<String, Object> searchVideo(
            @RequestParam String question,
            @RequestParam(defaultValue = "5") int limit) {

        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException(
                    "Question cannot be empty"
            );
        }

        if (limit <= 0 || limit > 50) {
            throw new IllegalArgumentException(
                    "Limit must be between 1 and 50"
            );
        }

        // Step 1: Generate question embedding
        float[] queryVector =
                embeddingService.generateVector(question);

        // Step 2: Search Qdrant
        List<ScoredPoint> results =
                searchService.searchSimilar(queryVector, limit);

        // Step 3: Convert results
        List<Map<String, Object>> segments = new ArrayList<>();

        for (ScoredPoint point : results) {

            Map<String, Object> data = new LinkedHashMap<>();

            data.put("pointId", point.getId().toString());
            data.put("similarityScore", point.getScore());

            Map<String, Object> payload = new LinkedHashMap<>();

            for (Map.Entry<String, Value> entry :
                    point.getPayloadMap().entrySet()) {

                payload.put(
                        entry.getKey(),
                        convertPayloadValue(entry.getValue())
                );
            }

            data.put("payload", payload);

            segments.add(data);
        }

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("question", question);
        response.put("totalResults", segments.size());
        response.put("results", segments);

        return response;
    }

    // Convert Qdrant Protobuf Value to normal Java object
    private Object convertPayloadValue(Value value) {

        switch (value.getKindCase()) {

            case STRING_VALUE:
                return value.getStringValue();

            case INTEGER_VALUE:
                return value.getIntegerValue();

            case DOUBLE_VALUE:
                return value.getDoubleValue();

            case BOOL_VALUE:
                return value.getBoolValue();

            case NULL_VALUE:
            case KIND_NOT_SET:
                return null;

            case STRUCT_VALUE:

                Map<String, Object> map = new LinkedHashMap<>();

                for (Map.Entry<String, Value> entry :
                        value.getStructValue().getFieldsMap().entrySet()) {

                    map.put(
                            entry.getKey(),
                            convertPayloadValue(entry.getValue())
                    );
                }

                return map;

            case LIST_VALUE:

                List<Object> list = new ArrayList<>();

                for (Value item :
                        value.getListValue().getValuesList()) {

                    list.add(convertPayloadValue(item));
                }

                return list;

            default:
                return null;
        }
    }
}
