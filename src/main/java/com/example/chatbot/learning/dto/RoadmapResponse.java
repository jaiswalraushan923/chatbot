package com.example.chatbot.learning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class RoadmapResponse {

    private UUID id;

    private String name;

    private List<TopicResponse> topics;
}