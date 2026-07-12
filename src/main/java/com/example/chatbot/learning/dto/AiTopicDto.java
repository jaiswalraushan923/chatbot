package com.example.chatbot.learning.dto;

import lombok.Data;

import java.util.List;

@Data
public class AiTopicDto {

    private String slug;

    private String title;

    private String description;

    private String difficulty;

    private Integer estimatedHours;

    private List<AiTopicDto> children;
}
