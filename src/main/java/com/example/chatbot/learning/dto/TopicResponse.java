package com.example.chatbot.learning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class TopicResponse {

    private UUID id;
    private Integer ordering;

    private String slug;
    private String title;
    private String description;

    private String difficulty;
    private Integer estimatedHours;

    private List<TopicResponse> children;

}
