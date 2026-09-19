package com.example.chatbot.learning;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ProjectSubmissionRepository extends JpaRepository<ProjectSubmission, UUID> {
    List<ProjectSubmission> findByUserId(UUID userId);
}
