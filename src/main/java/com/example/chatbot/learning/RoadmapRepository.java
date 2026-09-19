package com.example.chatbot.learning;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoadmapRepository extends JpaRepository<Roadmap, UUID> {

    @EntityGraph(attributePaths = {
            "roadmapTopics",
            "roadmapTopics.topic",
            "roadmapTopics.children",
            "roadmapTopics.children.topic"
    })
    Optional<Roadmap> findById(UUID id);

    @EntityGraph(attributePaths = {
            "roadmapTopics",
            "roadmapTopics.topic",
            "roadmapTopics.topic.parentTopic"
    })
    Optional<Roadmap> findByGoalId(UUID goalId);

}