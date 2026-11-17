
package com.example.productservice.transcription.service;

import java.util.Map;

public interface VideoContextBuilderService {

    Map<String, Object> buildContext(
            String videoId,
            int matchedSegment
    );
}
