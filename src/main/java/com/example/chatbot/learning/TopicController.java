package com.example.chatbot.learning;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/topics")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TopicController {

    private final TopicRepository topicRepository;

    @PostMapping("/create")
    public ResponseEntity<?> createTopic(@RequestBody Map<String, Object> request) {
        String slug = (String) request.get("slug");
        String title = (String) request.get("title");
        String description = (String) request.get("description");
        String difficulty = (String) request.getOrDefault("difficulty", "INTERMEDIATE");
        Integer estimatedHours = (Integer) request.getOrDefault("estimatedHours", 10);
        String parentId = (String) request.get("parentId");

        Topic topic = Topic.builder()
                .slug(slug)
                .title(title)
                .description(description)
                .difficulty(difficulty)
                .estimatedHours(estimatedHours)
                .build();

        if (parentId != null && !parentId.isEmpty()) {
            Topic parent = topicRepository.findById(UUID.fromString(parentId))
                    .orElseThrow(() -> new IllegalArgumentException("Parent topic not found"));
            topic.setParentTopic(parent);
        }

        Topic saved = topicRepository.save(topic);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/{topicId}")
    public ResponseEntity<?> getTopic(@PathVariable UUID topicId) {
        return topicRepository.findById(topicId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<?> getTopicBySlug(@PathVariable String slug) {
        return topicRepository.findBySlug(slug)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{topicId}/subtopics")
    public ResponseEntity<?> getSubtopics(@PathVariable UUID topicId) {
        List<Topic> subtopics = topicRepository.findByParentTopicId(topicId);
        return ResponseEntity.ok(subtopics);
    }

    @GetMapping("/root")
    public ResponseEntity<?> getRootTopics() {
        List<Topic> rootTopics = topicRepository.findByParentTopicIsNull();
        return ResponseEntity.ok(rootTopics);
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllTopics() {
        List<Topic> allTopics = topicRepository.findAll();
        return ResponseEntity.ok(allTopics);
    }

    @PutMapping("/{topicId}")
    public ResponseEntity<?> updateTopic(
            @PathVariable UUID topicId,
            @RequestBody Map<String, Object> request) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found"));

        if (request.containsKey("title")) {
            topic.setTitle((String) request.get("title"));
        }
        if (request.containsKey("description")) {
            topic.setDescription((String) request.get("description"));
        }
        if (request.containsKey("difficulty")) {
            topic.setDifficulty((String) request.get("difficulty"));
        }
        if (request.containsKey("estimatedHours")) {
            topic.setEstimatedHours((Integer) request.get("estimatedHours"));
        }

        Topic updated = topicRepository.save(topic);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{topicId}")
    public ResponseEntity<?> deleteTopic(@PathVariable UUID topicId) {
        topicRepository.deleteById(topicId);
        return ResponseEntity.ok(Map.of("message", "Topic deleted"));
    }
}
