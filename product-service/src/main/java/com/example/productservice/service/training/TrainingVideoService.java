package com.example.productservice.service.training;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface TrainingVideoService {

    Map<String, Object> uploadAndTranscribe(MultipartFile file);
}
