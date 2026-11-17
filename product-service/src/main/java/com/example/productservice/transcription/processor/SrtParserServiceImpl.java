
package com.example.productservice.transcription.processor;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SrtParserServiceImpl implements SrtParserService {

    private static final Pattern SRT_PATTERN = Pattern.compile(
            "(?m)^\\s*(\\d+)\\s*\\R" +
                    "(\\d{2}:\\d{2}:\\d{2}[,.]\\d{3})\\s*-->\\s*" +
                    "(\\d{2}:\\d{2}:\\d{2}[,.]\\d{3})\\s*\\R" +
                    "([\\s\\S]*?)(?=\\R\\s*\\R|\\z)"
    );

    @Override
    public List<Map<String, Object>> parseSrt(Path srtPath) {

        List<Map<String, Object>> segments = new ArrayList<>();

        try {

            if (!Files.isRegularFile(srtPath)) {
                throw new IllegalArgumentException(
                        "SRT file not found: " + srtPath
                );
            }

            String content = Files.readString(
                    srtPath, StandardCharsets.UTF_8
            ).replace("\uFEFF", "");

            Matcher matcher = SRT_PATTERN.matcher(content);

            while (matcher.find()) {

                int segment = Integer.parseInt(matcher.group(1));

                String startTime = matcher.group(2);
                String endTime = matcher.group(3);
                String text = matcher.group(4).trim()
                        .replaceAll("\\s+", " ");

                if (text.isEmpty()) {
                    continue;
                }

                double startSeconds = convertToSeconds(startTime);
                double endSeconds = convertToSeconds(endTime);

                Map<String, Object> data = new LinkedHashMap<>();

                data.put("segment", segment);
                data.put("startTime", startTime);
                data.put("endTime", endTime);
                data.put("startSeconds", startSeconds);
                data.put("endSeconds", endSeconds);
                double duration = Math.round( (endSeconds - startSeconds) * 1000.0) / 1000.0;
                data.put("duration", duration);
                data.put("text", text);

                segments.add(data);
            }

            System.out.println("TOTAL PARSED SEGMENTS: " + segments.size());

        } catch (IOException e) {
            throw new RuntimeException(
                    "Unable to read SRT file: " + srtPath, e
            );
        }

        return segments;
    }

    private double convertToSeconds(String time) {

        String[] parts = time.replace(",", ".").split(":");

        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        double seconds = Double.parseDouble(parts[2]);

        return hours * 3600 + minutes * 60 + seconds;
    }
}
