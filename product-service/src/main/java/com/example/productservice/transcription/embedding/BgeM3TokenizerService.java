package com.example.productservice.transcription.embedding;

import java.util.Map;

public interface BgeM3TokenizerService {

    Map<String, Object> tokenize(String text);
}