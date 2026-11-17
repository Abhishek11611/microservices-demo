package com.example.productservice.transcription.vector;

import io.qdrant.client.grpc.Points.ScoredPoint;
import io.qdrant.client.grpc.Points.RetrievedPoint;

import java.util.List;

public interface QdrantSearchService {

    List<ScoredPoint> searchSimilar(
            float[] queryVector,
            int limit
    );

    List<RetrievedPoint> searchByVideoId(String videoId);
}