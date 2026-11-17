
package com.example.productservice.transcription.vector;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Points.PointStruct;
import io.qdrant.client.grpc.JsonWithInt.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.qdrant.client.PointIdFactory.id;
import static io.qdrant.client.ValueFactory.value;
import static io.qdrant.client.VectorsFactory.vectors;

@Service
public class QdrantStorageServiceImpl implements QdrantStorageService {

    private final QdrantClient qdrantClient;

    private final String collectionName;

    public QdrantStorageServiceImpl(
            QdrantClient qdrantClient,
            @org.springframework.beans.factory.annotation.Value("${qdrant.collection-name}") String collectionName) {

        this.qdrantClient = qdrantClient;
        this.collectionName = collectionName;
    }

    // Save single embedding
    @Override
    public void saveEmbedding(
            String pointId,
            float[] vector,
            Map<String, Object> payload) {

        Map<String, Object> point = new HashMap<>();

        point.put("pointId", pointId);
        point.put("vector", vector);
        point.put("payload", payload);

        saveEmbeddings(List.of(point));
    }

    // Save multiple embeddings in one Qdrant request
    @Override
    public void saveEmbeddings(List<Map<String, Object>> points) {

        try {

            if (points == null || points.isEmpty()) {
                throw new IllegalArgumentException(
                        "Points list cannot be empty"
                );
            }

            List<PointStruct> qdrantPoints = new ArrayList<>();

            for (Map<String, Object> point : points) {

                String pointId = (String) point.get("pointId");

                float[] vector = (float[]) point.get("vector");

                Map<String, Object> payload =
                        (Map<String, Object>) point.get("payload");

                PointStruct qdrantPoint =
                        createPoint(pointId, vector, payload);

                qdrantPoints.add(qdrantPoint);
            }

            qdrantClient.upsertAsync(
                    collectionName,
                    qdrantPoints
            ).get();

            System.out.println(
                    "Total embeddings saved in Qdrant: "
                            + qdrantPoints.size()
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Qdrant batch storage interrupted", e
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Unable to save embeddings in Qdrant: "
                            + e.getMessage(), e
            );
        }
    }

    // Convert Java data into Qdrant PointStruct
    private PointStruct createPoint(
            String pointId,
            float[] vector,
            Map<String, Object> payload) {

        if (pointId == null || pointId.isBlank()) {
            throw new IllegalArgumentException(
                    "Point ID cannot be empty"
            );
        }

        if (vector == null || vector.length != 1024) {
            throw new IllegalArgumentException(
                    "Embedding must contain exactly 1024 dimensions"
            );
        }

        if (payload == null) {
            throw new IllegalArgumentException(
                    "Payload cannot be null"
            );
        }

        UUID uuid = UUID.fromString(pointId);

        Map<String, Value> qdrantPayload = new HashMap<>();

        for (Map.Entry<String, Object> entry : payload.entrySet()) {

            Object payloadValue = entry.getValue();

            if (payloadValue instanceof String) {

                qdrantPayload.put(
                        entry.getKey(),
                        value((String) payloadValue)
                );

            } else if (payloadValue instanceof Integer
                    || payloadValue instanceof Long) {

                qdrantPayload.put(
                        entry.getKey(),
                        value(((Number) payloadValue).longValue())
                );

            } else if (payloadValue instanceof Number) {

                qdrantPayload.put(
                        entry.getKey(),
                        value(((Number) payloadValue).doubleValue())
                );

            } else if (payloadValue instanceof Boolean) {

                qdrantPayload.put(
                        entry.getKey(),
                        value((Boolean) payloadValue)
                );

            } else if (payloadValue != null) {

                throw new IllegalArgumentException(
                        "Unsupported payload type for key: "
                                + entry.getKey()
                );
            }
        }

        return PointStruct.newBuilder()
                .setId(id(uuid))
                .setVectors(vectors(vector))
                .putAllPayload(qdrantPayload)
                .build();
    }
}
