
package com.example.productservice.transcription.service;

import com.example.productservice.transcription.embedding.BgeM3EmbeddingService;
import com.example.productservice.transcription.llm.OllamaService;
import com.example.productservice.transcription.vector.QdrantSearchService;

import io.qdrant.client.grpc.Points.ScoredPoint;
import io.qdrant.client.grpc.JsonWithInt.Value;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class VideoQuestionAnswerServiceImpl
        implements VideoQuestionAnswerService {

    private final BgeM3EmbeddingService embeddingService;
    private final QdrantSearchService searchService;
    private final VideoContextBuilderService contextBuilderService;
    private final OllamaService ollamaService;

    public VideoQuestionAnswerServiceImpl(
            BgeM3EmbeddingService embeddingService,
            QdrantSearchService searchService,
            VideoContextBuilderService contextBuilderService,
            OllamaService ollamaService) {

        this.embeddingService = embeddingService;
        this.searchService = searchService;
        this.contextBuilderService = contextBuilderService;
        this.ollamaService = ollamaService;
    }

    @Override
    public Map<String, Object> askQuestion(
            String question,
            int limit) {

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

        // Step 2: Search matching transcript segments
        List<ScoredPoint> results =
                searchService.searchSimilar(queryVector, limit);

        if (results == null || results.isEmpty()) {
            throw new RuntimeException(
                    "No relevant video content found"
            );
        }

        List<Map<String, Object>> videoAnswers =
                new ArrayList<>();

        // Store multiple contexts for each video
        Map<String, Map<String, Object>> videoContexts =
                new LinkedHashMap<>();

        // Step 3: Collect matching contexts
        for (ScoredPoint point : results) {

            Map<String, Value> payload = point.getPayloadMap();

            if (!payload.containsKey("videoId")
                    || !payload.containsKey("segment")) {
                continue;
            }

            String videoId =
                    payload.get("videoId").getStringValue();

            int matchedSegment =
                    (int) payload.get("segment").getIntegerValue();

            double similarityScore = point.getScore();

            Map<String, Object> context =
                    contextBuilderService.buildContext(
                            videoId,
                            matchedSegment
                    );

            if (context == null || context.isEmpty()) {
                continue;
            }

            // Get or create video context
            Map<String, Object> videoData =
                    videoContexts.get(videoId);

            if (videoData == null) {

                videoData = new LinkedHashMap<>();

                videoData.put(
                        "similarityScore",
                        similarityScore
                );

                videoData.put(
                        "contexts",
                        new ArrayList<Map<String, Object>>()
                );

                videoContexts.put(videoId, videoData);

            } else {

                double existingScore =
                        ((Number) videoData.get("similarityScore"))
                                .doubleValue();

                if (similarityScore > existingScore) {
                    videoData.put(
                            "similarityScore",
                            similarityScore
                    );
                }
            }

            List<Map<String, Object>> contexts =
                    (List<Map<String, Object>>)
                            videoData.get("contexts");

            contexts.add(context);
        }


       // Step 4: Generate answer and collect relevant timestamps

        for (Map.Entry<String, Map<String, Object>> entry :
                videoContexts.entrySet()) {

            String videoId = entry.getKey();

            Map<String, Object> videoData = entry.getValue();

            List<Map<String, Object>> contexts =
                    (List<Map<String, Object>>)
                            videoData.get("contexts");

            if (contexts.isEmpty()) {
                continue;
            }

            StringBuilder combinedContext = new StringBuilder();

            Set<String> uniqueTexts = new LinkedHashSet<>();

            List<Map<String, Object>> relevantTimestamps =
                    new ArrayList<>();

            for (Map<String, Object> context : contexts) {

                String contextText =
                        (String) context.get("contextText");

                if (contextText != null
                        && !contextText.isBlank()
                        && uniqueTexts.add(contextText.trim())) {

                    combinedContext
                            .append(contextText.trim())
                            .append("\n");
                }

                double startSeconds =
                        ((Number) context.get("startSeconds"))
                                .doubleValue();

                double endSeconds =
                        ((Number) context.get("endSeconds"))
                                .doubleValue();

                Map<String, Object> timestamp =
                        new LinkedHashMap<>();

                timestamp.put("startSeconds", startSeconds);
                timestamp.put("endSeconds", endSeconds);

                relevantTimestamps.add(timestamp);
            }

            if (combinedContext.isEmpty()) {
                continue;
            }

            // Generate answer
            String answer =
                    ollamaService.generateAnswer(
                            question,
                            combinedContext.toString()
                    );

            // Merge overlapping timestamp ranges
            List<Map<String, Object>> mergedTimestamps =
                    mergeTimestamps(relevantTimestamps);

            // Build final timestamp response
            List<Map<String, Object>> formattedTimestamps =
                    new ArrayList<>();

            for (Map<String, Object> timestamp : mergedTimestamps) {

                double startSeconds =
                        ((Number) timestamp.get("startSeconds"))
                                .doubleValue();

                double endSeconds =
                        ((Number) timestamp.get("endSeconds"))
                                .doubleValue();

                Map<String, Object> formatted =
                        new LinkedHashMap<>();

                formatted.put("start", formatTime(startSeconds));
                formatted.put("end", formatTime(endSeconds));

                formatted.put("startSeconds", startSeconds);
                formatted.put("endSeconds", endSeconds);

                formattedTimestamps.add(formatted);
            }

            Map<String, Object> videoResult =
                    new LinkedHashMap<>();

            videoResult.put("videoId", videoId);
            videoResult.put("answer", answer);

            videoResult.put(
                    "similarityScore",
                    videoData.get("similarityScore")
            );

            videoResult.put(
                    "relevantTimestamps",
                    formattedTimestamps
            );

            videoResult.put(
                    "totalSegments",
                    uniqueTexts.size()
            );

            videoAnswers.add(videoResult);
        }


        // Step 5: Final response
        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put("question", question);
        response.put("totalVideos", videoAnswers.size());
        response.put("results", videoAnswers);

        return response;
    }

    private String formatTime(double seconds) {

        long totalSeconds = (long) seconds;

        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long remainingSeconds = totalSeconds % 60;

        return String.format(
                "%02d:%02d:%02d",
                hours,
                minutes,
                remainingSeconds
        );
    }

    private String formatDuration(double seconds) {

        long totalSeconds = Math.round(seconds);

        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long remainingSeconds = totalSeconds % 60;

        return String.format(
                "%02d:%02d:%02d",
                hours,
                minutes,
                remainingSeconds
        );
    }


    private List<Map<String, Object>> mergeTimestamps(
            List<Map<String, Object>> timestamps) {

        List<Map<String, Object>> sorted =
                new ArrayList<>(timestamps);

        // Sort by start time
        Collections.sort(
                sorted,
                Comparator.comparingDouble(
                        timestamp ->
                                ((Number) timestamp.get("startSeconds"))
                                        .doubleValue()
                )
        );

        List<Map<String, Object>> merged =
                new ArrayList<>();

        for (Map<String, Object> current : sorted) {

            double currentStart =
                    ((Number) current.get("startSeconds"))
                            .doubleValue();

            double currentEnd =
                    ((Number) current.get("endSeconds"))
                            .doubleValue();

            if (merged.isEmpty()) {

                Map<String, Object> first =
                        new LinkedHashMap<>();

                first.put("startSeconds", currentStart);
                first.put("endSeconds", currentEnd);

                merged.add(first);

                continue;
            }

            Map<String, Object> previous =
                    merged.get(merged.size() - 1);

            double previousEnd =
                    ((Number) previous.get("endSeconds"))
                            .doubleValue();

            // Merge only overlapping ranges
            if (currentStart <= previousEnd) {

                previous.put(
                        "endSeconds",
                        Math.max(previousEnd, currentEnd)
                );

            } else {

                Map<String, Object> next =
                        new LinkedHashMap<>();

                next.put("startSeconds", currentStart);
                next.put("endSeconds", currentEnd);

                merged.add(next);
            }
        }

        return merged;
    }

}
