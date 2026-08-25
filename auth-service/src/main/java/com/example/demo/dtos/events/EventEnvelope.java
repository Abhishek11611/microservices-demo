package com.example.demo.dtos.events;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.UUID;

public record EventEnvelope(UUID eventId, String eventType,
                            String eventKey, JsonNode payload) {
}
