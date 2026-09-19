package com.example.chatbot.learning;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserTopicProgressRepository extends JpaRepository<UserTopicProgress, UUID> {
    List<UserTopicProgress> findByUserId(UUID userId);
    Optional<UserTopicProgress> findByUserIdAndTopicId(UUID userId, UUID topicId);
}
