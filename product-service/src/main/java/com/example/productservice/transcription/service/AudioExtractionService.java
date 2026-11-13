package com.example.productservice.transcription.service;

import java.nio.file.Path;

public interface AudioExtractionService {

    Path extractAudio(Path videoPath, String fileName);
}
