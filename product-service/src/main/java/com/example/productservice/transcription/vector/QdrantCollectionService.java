
package com.example.productservice.transcription.vector;

import com.example.productservice.transcription.config.QdrantConfig;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections.Distance;
import io.qdrant.client.grpc.Collections.VectorParams;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class QdrantCollectionService {

    private final QdrantClient qdrantClient;

    @Value("${qdrant.collection-name}")
    private String collectionName;

    @Value("${qdrant.vector-size}")
    private int vectorSize;

    public QdrantCollectionService(QdrantClient qdrantClient) {
        this.qdrantClient = qdrantClient;
    }

    public void createCollection() {

        try {

            boolean exists = qdrantClient
                    .collectionExistsAsync(collectionName)
                    .get();

            if (exists) {
                System.out.println(
                        "Qdrant collection already exists: " + collectionName
                );
                return;
            }

            qdrantClient.createCollectionAsync(
                    collectionName,
                    VectorParams.newBuilder()
                            .setSize(vectorSize)
                            .setDistance(Distance.Cosine)
                            .build()
            ).get();

            System.out.println(
                    "Qdrant collection created: " + collectionName
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Unable to create Qdrant collection: " + e.getMessage(), e
            );
        }
    }
}
