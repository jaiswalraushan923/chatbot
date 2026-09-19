package com.example.chatbot.learning;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProgressResponse(
        UUID id,
        UUID topicId,
        double proficiency,
        LocalDateTime lastAssessedAt,
        String notes
) {
}