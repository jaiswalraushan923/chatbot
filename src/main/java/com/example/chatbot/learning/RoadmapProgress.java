package com.example.chatbot.learning;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoadmapProgress {
    private UUID roadmapId;
    private int totalTopics;
    private int completedTopics;
    private double overallCoveragePercent;
    private double averageProficiency;
    private List<TopicProgressDTO> topicProgresses;
}
