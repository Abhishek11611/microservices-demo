
package com.example.productservice.service.training;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TrainingVideoServiceImpl implements TrainingVideoService {

    @Value("${training.video.upload-dir}")
    private String uploadDir;

    @Value("${training.audio.output-dir}")
    private String audioDir;

    @Value("${training.ffmpeg.path}")
    private String ffmpegPath;

    @Value("${training.whisper.model-path}")
    private String whisperModelPath;

    @Value("${training.whisper.language:en}")
    private String language;

    @Value("${training.whisper.working-dir:C:/}")
    private String whisperWorkingDir;

    @Override
    public Map<String, Object> uploadAndTranscribe(MultipartFile file) {

        Map<String, Object> response = new HashMap<>();

        try {

            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("Please upload a video");
            }

            String originalFilename = file.getOriginalFilename();

            if (originalFilename == null ||
                    !originalFilename.toLowerCase().endsWith(".mp4")) {
                throw new IllegalArgumentException("Only MP4 videos are supported");
            }

            Files.createDirectories(Path.of(uploadDir));
            Files.createDirectories(Path.of(audioDir));

            String uniqueId = UUID.randomUUID().toString();

            Path videoPath = Path.of(uploadDir, uniqueId + ".mp4");
            Path audioPath = Path.of(audioDir, uniqueId + ".mp3");
            Path subtitlePath = Path.of(audioDir, uniqueId + ".srt");

            // Step 1: Save video

            file.transferTo(videoPath);

            // Step 2: Extract audio

            ProcessBuilder audioBuilder = new ProcessBuilder(
                    ffmpegPath,
                    "-y",
                    "-i", videoPath.toString(),
                    "-map", "0:a:0",
                    "-vn",
                    "-ac", "1",
                    "-ar", "16000",
                    "-b:a", "64k",
                    "-codec:a", "libmp3lame",
                    audioPath.toString()
            );

            executeProcess(audioBuilder);

            // Step 3: Whisper transcription with timestamps

            String modelRelativePath = "whisper/models/ggml-base.en.bin";

            String subtitleRelativePath =
                    "ecommerce-training/audio/" + uniqueId + ".srt";

            String whisperFilter =
                    "whisper=model=" + modelRelativePath
                            + ":language=" + language
                            + ":use_gpu=0"
                            + ":destination=" + subtitleRelativePath
                            + ":format=srt";

            ProcessBuilder whisperBuilder = new ProcessBuilder(
                    ffmpegPath,
                    "-y",
                    "-i", audioPath.toString(),
                    "-af", whisperFilter,
                    "-f", "null",
                    "-"
            );

            whisperBuilder.directory(new File(whisperWorkingDir));

            executeProcess(whisperBuilder);

            // Step 4: Read SRT file

            String srtContent = Files.readString(
                    subtitlePath,
                    StandardCharsets.UTF_8
            );

            // Step 5: Parse sentence/segment timestamps

            List<Map<String, Object>> segments =
                    parseSrt(srtContent);

            if (segments.isEmpty()) {
                throw new RuntimeException("No transcript segments found");
            }

            // Step 6: Generate candidate chapters

            List<Map<String, Object>> chapters =
                    generateChapters(segments);

            // Step 7: Generate complete transcript

            StringBuilder transcript = new StringBuilder();

            for (Map<String, Object> segment : segments) {

                transcript.append(segment.get("text"));
                transcript.append(" ");
            }

            response.put("status", "SUCCESS");
            response.put("originalFileName", originalFilename);
            response.put("videoPath", videoPath.toString());
            response.put("audioPath", audioPath.toString());
            response.put("transcript", transcript.toString().trim());

            response.put("segments", segments);
            response.put("chapters", chapters);

            response.put("totalSegments", segments.size());
            response.put("totalChapters", chapters.size());

        } catch (Exception e) {

            response.put("status", "FAILED");
            response.put("message", e.getMessage());
        }

        return response;
    }

    // Execute FFmpeg process

    private void executeProcess(ProcessBuilder processBuilder)
            throws Exception {

        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        StringBuilder output = new StringBuilder();

        try (var reader = process.inputReader()) {

            String line;

            while ((line = reader.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
            }
        }

        boolean completed = process.waitFor(10, TimeUnit.MINUTES);

        if (!completed) {
            process.destroyForcibly();
            throw new RuntimeException("FFmpeg process timed out");
        }

        if (process.exitValue() != 0) {
            throw new RuntimeException("FFmpeg failed: " + output);
        }
    }

    // Parse SRT and generate timestamped segments

    private List<Map<String, Object>> parseSrt(String srtContent) {

        List<Map<String, Object>> segments = new ArrayList<>();

        Pattern pattern = Pattern.compile(
                "(?m)^\\d+\\s*\\R" +
                        "(\\d{2}:\\d{2}:\\d{2},\\d{3})\\s+-->\\s+" +
                        "(\\d{2}:\\d{2}:\\d{2},\\d{3})\\s*\\R" +
                        "([\\s\\S]*?)(?=\\R{2,}|\\z)"
        );

        Matcher matcher = pattern.matcher(srtContent);

        int index = 1;

        while (matcher.find()) {

            double startTime = parseTimestamp(matcher.group(1));
            double endTime = parseTimestamp(matcher.group(2));

            String text = matcher.group(3)
                    .replaceAll("\\R", " ")
                    .trim();

            if (text.isEmpty()) {
                continue;
            }

            Map<String, Object> segment = new HashMap<>();

            segment.put("segmentNumber", index++);
            segment.put("startTime", startTime);
            segment.put("endTime", endTime);
            segment.put("duration", endTime - startTime);
            segment.put("startFormatted", formatTime(startTime));
            segment.put("endFormatted", formatTime(endTime));
            segment.put("text", text);

            segments.add(segment);
        }

        return segments;
    }

    // Convert HH:mm:ss,SSS into seconds

    private double parseTimestamp(String timestamp) {

        String[] parts = timestamp.split("[:,]");

        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);
        int milliseconds = Integer.parseInt(parts[3]);

        return hours * 3600
                + minutes * 60
                + seconds
                + milliseconds / 1000.0;
    }

    // Convert seconds into HH:mm:ss

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

    // Generate candidate chapters from consecutive segments

    private List<Map<String, Object>> generateChapters(
            List<Map<String, Object>> segments) {

        List<Map<String, Object>> chapters = new ArrayList<>();

        List<Map<String, Object>> currentSegments = new ArrayList<>();

        double chapterStart = 0;
        double chapterEnd = 0;

        int chapterNumber = 1;

        for (Map<String, Object> segment : segments) {

            double start = (double) segment.get("startTime");
            double end = (double) segment.get("endTime");

            boolean hasGap = !currentSegments.isEmpty()
                    && start - chapterEnd >= 1.5;

            boolean maxDurationReached = !currentSegments.isEmpty()
                    && start - chapterStart >= 20;

            if (hasGap || maxDurationReached) {

                chapters.add(
                        createChapter(chapterNumber++, currentSegments,
                                chapterStart, chapterEnd)
                );

                currentSegments = new ArrayList<>();
            }

            if (currentSegments.isEmpty()) {
                chapterStart = start;
            }

            currentSegments.add(segment);
            chapterEnd = end;
        }

        if (!currentSegments.isEmpty()) {

            chapters.add(
                    createChapter(chapterNumber, currentSegments,
                            chapterStart, chapterEnd)
            );
        }

        return chapters;
    }

    // Create chapter response

    private Map<String, Object> createChapter(
            int chapterNumber,
            List<Map<String, Object>> segments,
            double startTime,
            double endTime) {

        Map<String, Object> chapter = new HashMap<>();

        String firstText = segments.get(0).get("text").toString();

        String title = firstText.length() > 60
                ? firstText.substring(0, 60) + "..."
                : firstText;

        StringBuilder chapterTranscript = new StringBuilder();

        for (Map<String, Object> segment : segments) {

            chapterTranscript.append(segment.get("text"));
            chapterTranscript.append(" ");
        }

        chapter.put("chapterNumber", chapterNumber);
        chapter.put("title", "Chapter " + chapterNumber + ": " + title);
        chapter.put("startTime", startTime);
        chapter.put("endTime", endTime);
        chapter.put("duration", endTime - startTime);
        chapter.put("startFormatted", formatTime(startTime));
        chapter.put("endFormatted", formatTime(endTime));
        chapter.put("transcript", chapterTranscript.toString().trim());
        chapter.put("segments", new ArrayList<>(segments));

        return chapter;
    }
}
