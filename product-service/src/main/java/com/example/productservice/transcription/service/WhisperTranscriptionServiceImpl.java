package com.example.productservice.transcription.service;

import com.example.productservice.transcription.config.TranscriptionProperties;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class WhisperTranscriptionServiceImpl
        implements WhisperTranscriptionService {

    private final TranscriptionProperties properties;

    public WhisperTranscriptionServiceImpl(
            TranscriptionProperties properties) {
        this.properties = properties;
    }

    @Override
    public Map<String, Object> transcribe(Path audioPath, String fileName) {

        try {

            if (!Files.exists(audioPath)) {
                throw new RuntimeException(
                        "Audio file not found: " + audioPath
                );
            }

            Path outputDirectory = Path.of(
                    properties.getOutput().getDir()
            );

            Files.createDirectories(outputDirectory);

            String outputPrefix = outputDirectory
                    .resolve(fileName)
                    .toString();

            List<String> command = new ArrayList<>();

            command.add(properties.getWhisper().getExecutable());

            command.add("-m");
            command.add(properties.getWhisper().getModelPath());

            command.add("-f");
            command.add(audioPath.toAbsolutePath().toString());

            command.add("-l");
            command.add(properties.getWhisper().getLanguage());

            if (properties.getWhisper().isTranslate()) {
                command.add("-tr");
            }

            command.add("-t");
            command.add(String.valueOf(
                    properties.getWhisper().getThreads()
            ));

            command.add("-bs");
            command.add(String.valueOf(
                    properties.getWhisper().getBeamSize()
            ));

            command.add("--prompt");
            command.add(
                    "Insurance, life insurance, general insurance, " +
                            "policyholder, premium, sum assured, nominee, " +
                            "maturity benefit, death benefit, endowment plan, " +
                            "term insurance, whole life insurance."
            );

            command.add("-osrt");
            command.add("-otxt");

            command.add("-of");
            command.add(outputPrefix);

            command.add("-ng");

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
                        "Whisper transcription failed: " + logs
                );
            }

            Path srtPath = Path.of(outputPrefix + ".srt");
            Path txtPath = Path.of(outputPrefix + ".txt");

            if (!Files.exists(srtPath) || !Files.exists(txtPath)) {
                throw new RuntimeException(
                        "Whisper output files were not generated. " + logs
                );
            }

            return Map.of(
                    "message", "Transcription completed successfully",
                    "srtPath", srtPath.toString(),
                    "txtPath", txtPath.toString(),
                    "logs", logs.toString()
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Unable to transcribe audio: " + e.getMessage(), e
            );
        }
    }
}
