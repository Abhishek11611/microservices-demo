
package com.example.productservice.transcription.vector;

import io.qdrant.client.QdrantClient;

import io.qdrant.client.grpc.Common;
import io.qdrant.client.grpc.Points.SearchPoints;
import io.qdrant.client.grpc.Points.ScoredPoint;
import io.qdrant.client.grpc.Points.RetrievedPoint;
import io.qdrant.client.grpc.Points.ScrollPoints;
import io.qdrant.client.grpc.Points.ScrollResponse;

import io.qdrant.client.grpc.Common.Filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static io.qdrant.client.ConditionFactory.matchKeyword;
import static io.qdrant.client.WithPayloadSelectorFactory.enable;

@Service
public class QdrantSearchServiceImpl implements QdrantSearchService {

    private final QdrantClient qdrantClient;
    private final String collectionName;

    public QdrantSearchServiceImpl(
            QdrantClient qdrantClient,
            @Value("${qdrant.collection-name}") String collectionName) {

        this.qdrantClient = qdrantClient;
        this.collectionName = collectionName;
    }

    @Override
    public List<ScoredPoint> searchSimilar(
            float[] queryVector,
            int limit) {

        try {

            if (queryVector == null || queryVector.length != 1024) {
                throw new IllegalArgumentException(
                        "Query vector must contain 1024 dimensions"
                );
            }

            if (limit <= 0) {
                throw new IllegalArgumentException(
                        "Search limit must be greater than zero"
                );
            }

            List<Float> vector = new ArrayList<>();

            for (float value : queryVector) {
                vector.add(value);
            }

            SearchPoints request = SearchPoints.newBuilder()
                    .setCollectionName(collectionName)
                    .addAllVector(vector)
                    .setLimit(limit)
                    .setWithPayload(enable(true))
                    .build();

            return qdrantClient.searchAsync(request).get();

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Qdrant search interrupted", e
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Unable to search video embeddings in Qdrant", e
            );
        }
    }

    @Override
    public List<RetrievedPoint> searchByVideoId(String videoId) {

        try {

            if (videoId == null || videoId.isBlank()) {
                throw new IllegalArgumentException(
                        "Video ID cannot be empty"
                );
            }

            List<RetrievedPoint> allPoints = new ArrayList<>();

            Filter filter = Filter.newBuilder()
                    .addMust(matchKeyword("videoId", videoId))
                    .build();


            Common.PointId offset = null;

            while (true) {

                ScrollPoints.Builder requestBuilder =
                        ScrollPoints.newBuilder()
                                .setCollectionName(collectionName)
                                .setFilter(filter)
                                .setLimit(100)
                                .setWithPayload(enable(true));

                if (offset != null) {
                    requestBuilder.setOffset(offset);
                }

                ScrollResponse response =
                        qdrantClient.scrollAsync(
                                requestBuilder.build()
                        ).get();

                // Add current page records
                allPoints.addAll(response.getResultList());

                // Check whether next page exists
                if (!response.hasNextPageOffset()) {
                    break;
                }

                // Set next page offset
                offset = response.getNextPageOffset();
            }

            Collections.sort(allPoints, new Comparator<RetrievedPoint>() {

                @Override
                public int compare(
                        RetrievedPoint first,
                        RetrievedPoint second) {

                    long firstSegment = first.getPayloadMap()
                            .get("segment").getIntegerValue();

                    long secondSegment = second.getPayloadMap()
                            .get("segment").getIntegerValue();

                    return Long.compare(firstSegment, secondSegment);
                }
            });

            return allPoints;

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Qdrant video search interrupted", e
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Unable to retrieve video segments from Qdrant", e
            );
        }
    }
}
