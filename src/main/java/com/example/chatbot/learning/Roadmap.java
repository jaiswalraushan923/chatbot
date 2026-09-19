package com.example.chatbot.learning;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "roadmaps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Roadmap {
    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id", nullable = false, unique = true)
    private Goal goal;

    private String name;

    private LocalDateTime createdAt;

    @OneToMany(
            mappedBy = "roadmap",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("ordering ASC")
    private List<RoadmapTopic> roadmapTopics;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
