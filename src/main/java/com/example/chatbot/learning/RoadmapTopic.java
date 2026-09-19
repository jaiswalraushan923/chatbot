package com.example.chatbot.learning;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "roadmap_topics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoadmapTopic {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roadmap_id", nullable = false)
    private Roadmap roadmap;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="parent_id")
    private RoadmapTopic parent;

    @OneToMany(mappedBy = "parent",
            cascade = CascadeType.ALL)
    @OrderBy("ordering ASC")
    private List<RoadmapTopic> children;

    private Integer ordering;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "isanable", nullable = false, columnDefinition = "boolean default true")
    @Builder.Default
    private boolean isanable = true;

    public boolean isIsanable() {
        return this.isanable;
    }
}

