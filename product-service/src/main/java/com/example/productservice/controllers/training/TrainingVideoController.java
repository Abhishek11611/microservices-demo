package com.example.productservice.controllers.training;

import com.example.productservice.service.training.TrainingVideoService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/training")
public class TrainingVideoController {

    private final TrainingVideoService trainingVideoService;

    public TrainingVideoController(TrainingVideoService trainingVideoService) {
        this.trainingVideoService = trainingVideoService;
    }

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Map<String, Object>> uploadVideo(
            @RequestParam("file") MultipartFile file) {

        Map<String, Object> response =
                trainingVideoService.uploadAndTranscribe(file);

        return ResponseEntity.ok(response);
    }

}
