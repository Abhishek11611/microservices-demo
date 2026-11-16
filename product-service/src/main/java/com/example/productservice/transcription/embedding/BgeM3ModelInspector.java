package com.example.productservice.transcription.embedding;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class BgeM3ModelInspector {

    @Value("${bge.model.directory}")
    private String modelDirectory;

    @Value("${bge.model.onnx-file}")
    private String onnxFile;

    public Map<String, Object> inspectModel() {

        Path modelPath = Path.of(modelDirectory, onnxFile);

        try (OrtSession.SessionOptions options =
                     new OrtSession.SessionOptions();

             OrtSession session = OrtEnvironment.getEnvironment()
                     .createSession(modelPath.toString(), options)) {

            Map<String, Object> result = new LinkedHashMap<>();

            result.put("modelPath", modelPath.toString());
            result.put("inputs", session.getInputInfo());
            result.put("outputs", session.getOutputInfo());

            return result;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Unable to inspect BGE-M3 ONNX model", e
            );
        }
    }
}