package com.example.chatbot.learning;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TopicRepository extends JpaRepository<Topic, UUID> {
    Optional<Topic> findBySlug(String slug);
    List<Topic> findByParentTopicId(UUID parentId);
    List<Topic> findByParentTopicIsNull();
}
