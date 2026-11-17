
package com.example.productservice.transcription.controller;

import com.example.productservice.transcription.service.VideoQuestionAnswerService;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/video")
public class VideoQuestionAnswerController {

    private final VideoQuestionAnswerService questionAnswerService;

    public VideoQuestionAnswerController(
            VideoQuestionAnswerService questionAnswerService) {

        this.questionAnswerService = questionAnswerService;
    }

    @GetMapping("/ask")
    public Map<String, Object> askQuestion(
            @RequestParam String question,
            @RequestParam(defaultValue = "5") int limit) {

        return questionAnswerService.askQuestion(
                question,
                limit
        );
    }
}
