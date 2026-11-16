
package com.example.productservice.transcription.vector;

import java.util.List;
import java.util.Map;

public interface QdrantStorageService {

    void saveEmbedding(
            String pointId,
            float[] vector,
            Map<String, Object> payload
    );

    void saveEmbeddings(List<Map<String, Object>> points);
}
