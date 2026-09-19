package com.example.chatbot.learning;

import com.example.chatbot.learning.dto.RoadmapResponse;
import com.example.chatbot.learning.mapper.RoadmapMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/roadmaps")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RoadmapController {

    private final RoadmapService roadmapService;
    private final ProgressService progressService;
    private final RoadmapRepository roadmapRepository;


//    @PostMapping("/generate")
//    public ResponseEntity<?> generateRoadmap(@RequestParam UUID goalId) {
//        try {
//            Roadmap roadmap = roadmapService.generateRoadmap(goalId);
//            return ResponseEntity.status(HttpStatus.CREATED).body(roadmap);
//        } catch (JsonProcessingException e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Map.of("error", "Failed to generate roadmap: " + e.getMessage()));
//        }
//    }



    @GetMapping("/{roadmapId}")
    public ResponseEntity<RoadmapResponse> getRoadmap(
            @PathVariable UUID roadmapId) {

        return ResponseEntity.ok(
                roadmapService.getRoadmap(roadmapId)
        );
    }

    @GetMapping("/goal/{goalId}")
    public ResponseEntity<RoadmapResponse> getRoadmapByGoalId(
            @PathVariable UUID goalId) {

        return ResponseEntity.ok(
                roadmapService.getRoadmapByGoalId(goalId)
        );
    }



    @PostMapping("/{roadmapId}/topics")
    public ResponseEntity<?> addTopicToRoadmap(
            @PathVariable UUID roadmapId,
            @RequestBody Map<String, Object> request) {
        try {
            UUID topicId = UUID.fromString((String) request.get("topicId"));
            UUID parentId = null;
            if (request.get("parentId") != null) {
                parentId = UUID.fromString((String) request.get("parentId"));
            }
            Integer ordering = (Integer) request.get("ordering");
            String notes = (String) request.get("notes");

            roadmapService.addTopicToRoadmap(roadmapId, topicId, parentId, ordering, notes);
            return ResponseEntity.ok(Map.of("message", "Topic added to roadmap"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/topics/{roadmapTopicId}")
    public ResponseEntity<?> removeTopicFromRoadmap(@PathVariable UUID roadmapTopicId) {
        roadmapService.removeTopicFromRoadmap(roadmapTopicId);
        return ResponseEntity.ok(Map.of("message", "Topic removed from roadmap"));
    }

    @GetMapping("/{roadmapId}/progress")
    public ResponseEntity<?> getRoadmapProgress(
            @PathVariable UUID roadmapId,
            @RequestParam UUID userId) {
        RoadmapProgress progress = progressService.getRoadmapProgress(userId, roadmapId);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/{roadmapId}/next-topics")
    public ResponseEntity<?> getNextTopics(
            @PathVariable UUID roadmapId,
            @RequestParam UUID userId) {
        var nextTopics = progressService.getRecommendedNextTopics(roadmapId, userId);
        return ResponseEntity.ok(nextTopics);
    }
}
