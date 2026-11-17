
package com.example.productservice.transcription.service;

import com.example.productservice.transcription.vector.QdrantSearchService;

import io.qdrant.client.grpc.Points.RetrievedPoint;
import io.qdrant.client.grpc.JsonWithInt.Value;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class VideoContextBuilderServiceImpl
        implements VideoContextBuilderService {

    private final QdrantSearchService searchService;

    private static final int INITIAL_CONTEXT = 3;
    private static final int MAX_EXPANSION = 10;

    public VideoContextBuilderServiceImpl(
            QdrantSearchService searchService) {

        this.searchService = searchService;
    }

    @Override
    public Map<String, Object> buildContext(
            String videoId,
            int matchedSegment) {

        List<RetrievedPoint> allSegments =
                searchService.searchByVideoId(videoId);

        if (allSegments == null || allSegments.isEmpty()) {
            throw new RuntimeException(
                    "No transcript found for video: " + videoId
            );
        }

        int matchedIndex = -1;

        // Find matching segment
        for (int i = 0; i < allSegments.size(); i++) {

            long segmentNumber =
                    getLong(allSegments.get(i), "segment");

            if (segmentNumber == matchedSegment) {
                matchedIndex = i;
                break;
            }
        }

        if (matchedIndex == -1) {
            throw new RuntimeException(
                    "Matched segment not found: " + matchedSegment
            );
        }

        // Initial context
        int from = Math.max(
                0, matchedIndex - INITIAL_CONTEXT
        );

        int to = Math.min(
                allSegments.size() - 1,
                matchedIndex + INITIAL_CONTEXT
        );

        // Expand backward until sentence boundary
        int expansion = 0;

        while (from > 0 && expansion < MAX_EXPANSION) {

            String previousText =
                    getText(allSegments.get(from - 1));

            if (endsSentence(previousText)) {
                break;
            }

            from--;
            expansion++;
        }

        // Expand forward until sentence boundary
        expansion = 0;

        while (to < allSegments.size() - 1
                && expansion < MAX_EXPANSION) {

            String currentText =
                    getText(allSegments.get(to));

            if (endsSentence(currentText)) {
                break;
            }

            to++;
            expansion++;
        }

        // Build final context
        StringBuilder contextText = new StringBuilder();

        List<Map<String, Object>> contextSegments =
                new ArrayList<>();

        double startSeconds = Double.MAX_VALUE;
        double endSeconds = 0;

        for (int i = from; i <= to; i++) {

            RetrievedPoint point = allSegments.get(i);

            String text = getText(point);

            double segmentStart =
                    getDouble(point, "startSeconds");

            double segmentEnd =
                    getDouble(point, "endSeconds");

            if (contextText.length() > 0) {
                contextText.append(" ");
            }

            contextText.append(text);

            startSeconds = Math.min(
                    startSeconds, segmentStart
            );

            endSeconds = Math.max(
                    endSeconds, segmentEnd
            );

            Map<String, Object> segment =
                    new LinkedHashMap<>();

            segment.put("segment", getLong(point, "segment"));
            segment.put("text", text);
            segment.put("startSeconds", segmentStart);
            segment.put("endSeconds", segmentEnd);
            segment.put("startTime", getString(point, "startTime"));
            segment.put("endTime", getString(point, "endTime"));

            contextSegments.add(segment);
        }

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put("videoId", videoId);
        response.put("matchedSegment", matchedSegment);
        response.put("contextText", contextText.toString());

        response.put("startSeconds", startSeconds);
        response.put("endSeconds", endSeconds);
        response.put("duration", endSeconds - startSeconds);

        response.put("totalSegments", contextSegments.size());
        response.put("segments", contextSegments);

        return response;
    }

    private boolean endsSentence(String text) {

        if (text == null || text.isBlank()) {
            return false;
        }

        String cleaned = text.trim();

        return cleaned.matches(".*[.!?]+[\"')\\]]*$");
    }

    private String getText(RetrievedPoint point) {

        return getString(point, "text");
    }

    private String getString(
            RetrievedPoint point,
            String key) {

        Value value = point.getPayloadMap().get(key);

        if (value == null) {
            return "";
        }

        return value.getStringValue();
    }

    private long getLong(
            RetrievedPoint point,
            String key) {

        return point.getPayloadMap()
                .get(key)
                .getIntegerValue();
    }

    private double getDouble(
            RetrievedPoint point,
            String key) {

        return point.getPayloadMap()
                .get(key)
                .getDoubleValue();
    }
}
