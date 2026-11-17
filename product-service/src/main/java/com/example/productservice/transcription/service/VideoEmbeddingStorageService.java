
package com.example.productservice.transcription.service;

import com.example.productservice.transcription.processor.SrtParserService;
import com.example.productservice.transcription.embedding.BgeM3EmbeddingService;
import com.example.productservice.transcription.vector.QdrantStorageService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.*;

@Service
public class VideoEmbeddingStorageService {

    private final SrtParserService srtParserService;
    private final BgeM3EmbeddingService embeddingService;
    private final QdrantStorageService qdrantStorageService;

    private final String outputDirectory;

    private static final int BATCH_SIZE = 50;

    public VideoEmbeddingStorageService(
            SrtParserService srtParserService,
            BgeM3EmbeddingService embeddingService,
            QdrantStorageService qdrantStorageService,
            @Value("${transcription.output.dir}") String outputDirectory) {

        this.srtParserService = srtParserService;
        this.embeddingService = embeddingService;
        this.qdrantStorageService = qdrantStorageService;
        this.outputDirectory = outputDirectory;
    }

    public Map<String, Object> storeVideoEmbeddings(
            String videoId,
            String fileName) {

        if (videoId == null || videoId.isBlank()) {
            throw new IllegalArgumentException("Video ID is required");
        }

        if (fileName == null || fileName.isBlank()
                || !fileName.toLowerCase().endsWith(".srt")
                || !Path.of(fileName).getFileName().toString().equals(fileName)) {
            throw new IllegalArgumentException("Invalid SRT file name");
        }

        Path directory = Path.of(outputDirectory).toAbsolutePath().normalize();
        Path srtPath = directory.resolve(fileName).normalize();

        if (!srtPath.startsWith(directory)) {
            throw new IllegalArgumentException("Invalid SRT file path");
        }

        // Step 1: Parse SRT
        List<Map<String, Object>> segments =
                srtParserService.parseSrt(srtPath);

        if (segments.isEmpty()) {
            throw new IllegalArgumentException(
                    "No valid segments found in SRT file"
            );
        }

        int totalSegments = segments.size();
        int totalSaved = 0;

        List<Map<String, Object>> batch = new ArrayList<>();

        // Step 2: Generate embeddings
        for (Map<String, Object> segment : segments) {

            String text = (String) segment.get("text");

            float[] vector =
                    embeddingService.generateVector(text);

            // Step 3: Prepare payload
            Map<String, Object> payload = new LinkedHashMap<>();

            payload.put("videoId", videoId);
            payload.put("segment", segment.get("segment"));
            payload.put("startTime", segment.get("startTime"));
            payload.put("endTime", segment.get("endTime"));
            payload.put("startSeconds", segment.get("startSeconds"));
            payload.put("endSeconds", segment.get("endSeconds"));
            payload.put("duration", segment.get("duration"));
            payload.put("text", text);
            payload.put("model", "BAAI/bge-m3");

            // Step 4: Prepare Qdrant point
            Map<String, Object> point = new HashMap<>();

            point.put("pointId", UUID.randomUUID().toString());
            point.put("vector", vector);
            point.put("payload", payload);

            batch.add(point);

            // Step 5: Save when batch is full
            if (batch.size() == BATCH_SIZE) {

                qdrantStorageService.saveEmbeddings(batch);

                totalSaved += batch.size();
                batch.clear();
            }
        }

        // Save remaining segments
        if (!batch.isEmpty()) {

            qdrantStorageService.saveEmbeddings(batch);

            totalSaved += batch.size();
        }

        // Step 6: Return result
        Map<String, Object> response = new LinkedHashMap<>();

        response.put("videoId", videoId);
        response.put("fileName", fileName);
        response.put("totalSegments", totalSegments);
        response.put("totalSaved", totalSaved);
        response.put("batchSize", BATCH_SIZE);
        response.put("message", "Video embeddings stored successfully");

        return response;
    }
}
