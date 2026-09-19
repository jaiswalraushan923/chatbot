package com.example.chatbot.learning;

import lombok.*;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillCoverageReport {
    private UUID userId;
    private int totalTopicsTracked;
    private int masteredTopics;      // 80%+
    private int proficientTopics;    // 50-79%
    private int beginnerTopics;      // <50%
    private double overallSkillPercentage;
}
