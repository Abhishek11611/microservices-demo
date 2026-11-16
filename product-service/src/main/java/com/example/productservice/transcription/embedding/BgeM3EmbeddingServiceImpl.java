
package com.example.productservice.transcription.embedding;

import ai.onnxruntime.*;

import jakarta.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
public class BgeM3EmbeddingServiceImpl implements BgeM3EmbeddingService {

    private final BgeM3TokenizerService tokenizerService;

    private final OrtEnvironment environment;

    private final OrtSession session;

    public BgeM3EmbeddingServiceImpl(
            BgeM3TokenizerService tokenizerService,
            @Value("${bge.model.directory}") String modelDirectory,
            @Value("${bge.model.onnx-file}") String onnxFile) throws Exception {

        this.tokenizerService = tokenizerService;

        environment = OrtEnvironment.getEnvironment();

        Path modelPath = Path.of(modelDirectory, onnxFile);

        OrtSession.SessionOptions options =
                new OrtSession.SessionOptions();

        try {
            session = environment.createSession(
                    modelPath.toString(),
                    options
            );
        } finally {
            options.close();
        }
    }

    @Override
    public Map<String, Object> generateEmbedding(String text) {

        float[] vector = generateVector(text);

        double norm = 0;

        for (float value : vector) {
            norm += value * value;
        }

        norm = Math.sqrt(norm);

        Map<String, Object> response = new HashMap<>();

        response.put("dimensions", vector.length);
        response.put("vectorNorm", norm);
        response.put("vector", vector);

        response.put(
                "first10Values",
                Arrays.copyOf(vector, Math.min(10, vector.length))
        );

        Map<String, Object> tokens =
                tokenizerService.tokenize(text);

        long[] inputIds = (long[]) tokens.get("inputIds");

        response.put("tokenCount", inputIds.length);

        return response;
    }

    @Override
    public float[] generateVector(String text) {

        try {

            Map<String, Object> tokens =
                    tokenizerService.tokenize(text);

            long[] inputIds =
                    (long[]) tokens.get("inputIds");

            long[] attentionMask =
                    (long[]) tokens.get("attentionMask");

            try (
                    OnnxTensor inputTensor =
                            OnnxTensor.createTensor(
                                    environment,
                                    new long[][]{inputIds}
                            );

                    OnnxTensor attentionTensor =
                            OnnxTensor.createTensor(
                                    environment,
                                    new long[][]{attentionMask}
                            );

                    OrtSession.Result result = session.run(
                            Map.of(
                                    "input_ids", inputTensor,
                                    "attention_mask", attentionTensor
                            )
                    )
            ) {

                OnnxTensor output =
                        (OnnxTensor) result.get("sentence_embedding")
                                .orElseThrow();

                float[][] embedding =
                        (float[][]) output.getValue();

                float[] vector = embedding[0];

                if (vector.length != 1024) {
                    throw new IllegalStateException(
                            "Invalid embedding dimension: " + vector.length
                    );
                }

                return vector;
            }

        } catch (Exception e) {

            throw new RuntimeException(
                    "Unable to generate BGE-M3 embedding", e
            );
        }
    }

    @PreDestroy
    public void cleanup() throws Exception {
        session.close();
    }
}
