package com.example.chatbot.learning.mapper;

import com.example.chatbot.learning.Roadmap;
import com.example.chatbot.learning.RoadmapTopic;
import com.example.chatbot.learning.dto.RoadmapResponse;
import com.example.chatbot.learning.dto.TopicResponse;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class RoadmapMapper {

    public RoadmapResponse toResponse(Roadmap roadmap) {

        List<TopicResponse> topics = roadmap.getRoadmapTopics()
                .stream()
                .filter(rt -> rt.getParent() == null && rt.isIsanable())
                .sorted(Comparator.comparing(RoadmapTopic::getOrdering))
                .map(this::mapTopic)
                .toList();

        return RoadmapResponse.builder()
                .id(roadmap.getId())
                .name(roadmap.getName())
                .topics(topics)
                .build();
    }

    private TopicResponse mapTopic(RoadmapTopic rt) {

        List<TopicResponse> children = rt.getChildren()
                .stream()
                .filter(RoadmapTopic::isIsanable)
                .sorted(Comparator.comparing(RoadmapTopic::getOrdering))
                .map(this::mapTopic)
                .toList();

        return TopicResponse.builder()
                .id(rt.getId())
                .ordering(rt.getOrdering())
                .slug(rt.getTopic().getSlug())
                .title(rt.getTopic().getTitle())
                .description(rt.getTopic().getDescription())
                .difficulty(rt.getTopic().getDifficulty())
                .estimatedHours(rt.getTopic().getEstimatedHours())
                .children(children)
                .build();
    }
}

