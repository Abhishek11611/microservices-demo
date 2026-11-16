package com.example.productservice.transcription.embedding;

import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.djl.huggingface.tokenizers.Encoding;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class BgeM3TokenizerServiceImpl implements BgeM3TokenizerService {

    @Value("${bge.model.directory}")
    private String modelDirectory;

    @Value("${bge.model.tokenizer-file}")
    private String tokenizerFile;

    private HuggingFaceTokenizer tokenizer;

    @PostConstruct
    public void initialize() throws IOException {

        Path tokenizerPath = Path.of(
                modelDirectory,
                tokenizerFile
        );

        tokenizer = HuggingFaceTokenizer.newInstance(tokenizerPath);
    }

    @Override
    public Map<String, Object> tokenize(String text) {

        Encoding encoding = tokenizer.encode(text);

        long[] inputIds = encoding.getIds();
        long[] attentionMask = encoding.getAttentionMask();

        Map<String, Object> result = new LinkedHashMap<>();

        result.put("inputIds", inputIds);
        result.put("attentionMask", attentionMask);
        result.put("tokenCount", inputIds.length);

        return result;
    }
}