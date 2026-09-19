package com.example.chatbot.learning;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "topics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Topic {
    @Id
    @GeneratedValue
    private UUID id;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Topic parentTopic;

//    @OneToMany(mappedBy = "parentTopic", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Topic> subtopics;

    @Column(unique = true, nullable = false)
    private String slug;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String difficulty;

    private Integer estimatedHours;

//    private Integer ordering;

    private LocalDateTime createdAt;

//    @PrePersist
//    public void prePersist() {
//        this.createdAt = LocalDateTime.now();
//        if (this.ordering == null) this.ordering = 0;
//    }
}
