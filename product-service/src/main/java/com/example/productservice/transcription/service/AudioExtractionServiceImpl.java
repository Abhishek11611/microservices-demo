package com.example.productservice.transcription.service;

import com.example.productservice.transcription.config.TranscriptionProperties;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class AudioExtractionServiceImpl implements AudioExtractionService {

    private final TranscriptionProperties properties;

    public AudioExtractionServiceImpl(
            TranscriptionProperties properties) {
        this.properties = properties;
    }

    @Override
    public Path extractAudio(Path videoPath, String fileName) {

        try {
            Path audioDirectory = Path.of(
                    properties.getAudio().getOutputDir()
            );

            Files.createDirectories(audioDirectory);

            Path audioPath = audioDirectory.resolve(fileName + ".wav");

            List<String> command = List.of(
                    properties.getFfmpeg().getExecutable(),
                    "-y",
                    "-i", videoPath.toString(),
                    "-vn",
                    "-ac", "1",
                    "-ar", "16000",
                    "-c:a", "pcm_s16le",
                    audioPath.toString()
            );

            ProcessBuilder processBuilder =
                    new ProcessBuilder(command);

            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            StringBuilder logs = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {

                String line;

                while ((line = reader.readLine()) != null) {
                    logs.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException(
                        "FFmpeg audio extraction failed: " + logs
                );
            }

            if (!Files.exists(audioPath)
                    || Files.size(audioPath) == 0) {
                throw new RuntimeException(
                        "Audio file was not generated"
                );
            }

            return audioPath;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Unable to extract audio: " + e.getMessage(), e
            );
        }
    }
}
