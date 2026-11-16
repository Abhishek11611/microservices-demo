
package com.example.productservice.transcription.controller;

import com.example.productservice.transcription.embedding.BgeM3EmbeddingService;
import com.example.productservice.transcription.embedding.BgeM3ModelInspector;
import com.example.productservice.transcription.embedding.BgeM3TokenizerService;
import com.example.productservice.transcription.processor.SrtParserService;
import com.example.productservice.transcription.service.AudioExtractionService;
import com.example.productservice.transcription.service.VideoEmbeddingStorageService;
import com.example.productservice.transcription.service.WhisperTranscriptionService;

import com.example.productservice.transcription.vector.QdrantCollectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audio")
public class AudioExtractionController {

    private final AudioExtractionService audioExtractionService;
    private final WhisperTranscriptionService whisperTranscriptionService;
    private final QdrantCollectionService qdrantCollectionService;
    private final SrtParserService srtParserService;
    private final BgeM3ModelInspector bgeM3ModelInspector;
    private final BgeM3TokenizerService bgeM3TokenizerService;
    private final BgeM3EmbeddingService bgeM3EmbeddingService;
    private final VideoEmbeddingStorageService videoEmbeddingStorageService;

    public AudioExtractionController(
            AudioExtractionService audioExtractionService,
            WhisperTranscriptionService whisperTranscriptionService, QdrantCollectionService qdrantCollectionService, SrtParserService srtParserService, BgeM3ModelInspector bgeM3ModelInspector, BgeM3TokenizerService bgeM3TokenizerService, BgeM3EmbeddingService bgeM3EmbeddingService, VideoEmbeddingStorageService videoEmbeddingStorageService) {

        this.audioExtractionService = audioExtractionService;
        this.whisperTranscriptionService = whisperTranscriptionService;
        this.qdrantCollectionService = qdrantCollectionService;
        this.srtParserService = srtParserService;
        this.bgeM3ModelInspector = bgeM3ModelInspector;
        this.bgeM3TokenizerService = bgeM3TokenizerService;
        this.bgeM3EmbeddingService = bgeM3EmbeddingService;
        this.videoEmbeddingStorageService = videoEmbeddingStorageService;
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

    @PostMapping("/qdrant/create-collection")
    public ResponseEntity<Map<String, Object>> createCollection() {

        qdrantCollectionService.createCollection();

        return ResponseEntity.ok(
                Map.of(
                        "message", "Qdrant collection is ready"
                )
        );
    }

     // SRT Parser
    @PostMapping("/srt-parser")
    public ResponseEntity<Map<String, Object>> srtParser(@RequestParam String srtFilePath) {

        List<Map<String, Object>> segments = srtParserService.parseSrt(Path.of(srtFilePath));

        Map<String, Object> response = new HashMap<>();

        response.put("message", "SRT parsed successfully");
        response.put("totalSegments", segments.size());
        response.put("data", segments);


        return ResponseEntity.ok(response);
    }


    @GetMapping("/bge-model-info")
    public Map<String, Object> getBgeModelInfo() {
        return bgeM3ModelInspector.inspectModel();
    }

    @GetMapping("/bge-tokenize")
    public Map<String, Object> tokenize(
            @RequestParam String text) {

        return bgeM3TokenizerService.tokenize(text);
    }

    @GetMapping("/bge-embedding")
    public Map<String, Object> generateEmbedding(
            @RequestParam String text) {

        return bgeM3EmbeddingService.generateEmbedding(text);
    }

    @PostMapping("/srt-process-db")
    public Map<String, Object> generateEmbedding(
            @RequestParam String videoId,
            @RequestParam String fileName) {

        return videoEmbeddingStorageService.storeVideoEmbeddings(videoId,fileName);
    }


}
