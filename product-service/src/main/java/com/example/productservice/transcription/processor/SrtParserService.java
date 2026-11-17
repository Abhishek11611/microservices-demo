package com.example.productservice.transcription.processor;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface SrtParserService {

    List<Map<String, Object>> parseSrt(Path srtPath);

}