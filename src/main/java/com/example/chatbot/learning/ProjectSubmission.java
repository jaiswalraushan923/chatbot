package com.example.chatbot.learning;

import com.example.chatbot.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "project_submissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectSubmission {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String link;

    private LocalDateTime createdAt;

    private Integer assessmentScore;

    @Column(columnDefinition = "TEXT")
    private String assessmentNotes;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
