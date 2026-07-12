package com.example.chatbot.learning.mapper;

import com.example.chatbot.learning.Roadmap;
import com.example.chatbot.learning.dto.RoadmapResponse;
import com.example.chatbot.learning.dto.TopicResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RoadmapMapper {

    public RoadmapResponse toResponse(Roadmap roadmap) {
        System.out.println("Mapper started");

        List<TopicResponse> topics = roadmap.getRoadmapTopics()
                .stream()
                .map(rt -> TopicResponse.builder()
                        .id(rt.getTopic().getId())
                        .ordering(rt.getOrdering())
                        .slug(rt.getTopic().getSlug())
                        .title(rt.getTopic().getTitle())
                        .description(rt.getTopic().getDescription())
                        .difficulty(rt.getTopic().getDifficulty())
                        .estimatedHours(rt.getTopic().getEstimatedHours())
                        .build())
                .toList();
        System.out.println("Mapper finished");

        return RoadmapResponse.builder()
                .id(roadmap.getId())
                .name(roadmap.getName())
                .topics(topics)
                .build();
    }
}