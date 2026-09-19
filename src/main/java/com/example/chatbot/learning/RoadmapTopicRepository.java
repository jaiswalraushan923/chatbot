package com.example.chatbot.learning;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface RoadmapTopicRepository extends JpaRepository<RoadmapTopic, UUID> {
    List<RoadmapTopic> findByRoadmapIdOrderByOrdering(UUID roadmapId);
    void deleteByRoadmapId(UUID roadmapId);
    List<RoadmapTopic> findByRoadmapIdAndParentIsNullOrderByOrdering(UUID roadmapId);
    List<RoadmapTopic> findByRoadmapIdAndParentIdOrderByOrdering(UUID roadmapId, UUID parentId);
}
