
package com.example.productservice.transcription.controller;

import com.example.productservice.transcription.service.AudioExtractionService;
import com.example.productservice.transcription.service.WhisperTranscriptionService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/audio")
public class AudioExtractionController {

    private final AudioExtractionService audioExtractionService;
    private final WhisperTranscriptionService whisperTranscriptionService;

    public AudioExtractionController(
            AudioExtractionService audioExtractionService,
            WhisperTranscriptionService whisperTranscriptionService) {

        this.audioExtractionService = audioExtractionService;
        this.whisperTranscriptionService = whisperTranscriptionService;
    }

    // Only audio extraction
    @PostMapping("/extract")
    public ResponseEntity<Map<String, Object>> extractAudio(
            @RequestParam String videoPath,
            @RequestParam String fileName) {

        Path audioPath = audioExtractionService.extractAudio(
                Path.of(videoPath),
                fileName
        );

        return ResponseEntity.ok(
                Map.of(
                        "message", "Audio extracted successfully",
                        "audioPath", audioPath.toString()
                )
        );
    }

    // Audio extraction + transcription
    @PostMapping("/transcribe")
    public ResponseEntity<Map<String, Object>> transcriptAudio(
            @RequestParam String videoPath,
            @RequestParam String fileName) {

        // Step 1: Extract audio
        Path audioPath = audioExtractionService.extractAudio(
                Path.of(videoPath),
                fileName
        );

        // Step 2: Transcribe extracted audio
        Map<String, Object> transcription =
                whisperTranscriptionService.transcribe(
                        audioPath,
                        fileName
                );

        Map<String, Object> response = new HashMap<>();

        response.put("message", "Audio transcription completed successfully");
        response.put("audioPath", audioPath.toString());
        response.putAll(transcription);

        return ResponseEntity.ok(response);
    }
}
