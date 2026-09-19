package com.example.chatbot.learning;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicProgressDTO {
    private UUID topicId;
    private String topicTitle;
    private double proficiency;
    private LocalDateTime lastAssessedAt;
}
