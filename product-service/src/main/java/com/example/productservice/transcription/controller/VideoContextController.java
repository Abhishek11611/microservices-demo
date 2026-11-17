
package com.example.productservice.transcription.controller;

import com.example.productservice.transcription.service.VideoContextBuilderService;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/video")
public class VideoContextController {

    private final VideoContextBuilderService contextBuilderService;

    public VideoContextController(
            VideoContextBuilderService contextBuilderService) {
        this.contextBuilderService = contextBuilderService;
    }

    @GetMapping("/context")
    public Map<String, Object> getVideoContext(
            @RequestParam String videoId,
            @RequestParam int matchedSegment) {

        return contextBuilderService.buildContext(
                videoId,
                matchedSegment
        );
    }
}
