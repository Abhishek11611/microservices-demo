package com.example.productservice.transcription.service;

import java.nio.file.Path;
import java.util.Map;

public interface WhisperTranscriptionService {

    Map<String, Object> transcribe(Path audioPath, String fileName);
}
