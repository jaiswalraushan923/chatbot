package com.example.chatbot.learning;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/progress")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProgressController {

    private final ProgressService progressService;
    private final UserTopicProgressRepository userTopicProgressRepository;
    private ProgressResponse progressResponse;

    @PostMapping("/update-topic")
    public ResponseEntity<?> updateTopicProgress(
            @RequestParam UUID userId,
            @RequestParam UUID topicId,
            @RequestBody Map<String, Object> request) {
        Double proficiency = ((Number) request.get("proficiency")).doubleValue();
        String notes = (String) request.get("notes");

         progressResponse = progressService.updateTopicProficiency(userId, topicId, proficiency, notes);
        return ResponseEntity.ok(progressResponse);
    }

//    @GetMapping("/user/{userId}")
//    public ResponseEntity<?> getUserProgress(@PathVariable UUID userId) {
//        var progress = userTopicProgressRepository.findByUserId(userId);
//        return ResponseEntity.ok(progress);
//    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserProgress(@PathVariable UUID userId) {

        List<UserTopicProgress> progress =
                userTopicProgressRepository.findByUserId(userId);

        List<ProgressResponse> progressResponse = progress.stream()
                .map(p -> new ProgressResponse(
                        p.getId(),
                        p.getTopic().getId(),
                        p.getProficiency(),
                        p.getLastAssessedAt(),
                        p.getNotes()
                ))
                .toList();

        return ResponseEntity.ok(progressResponse);
    }

    @GetMapping("/user/{userId}/coverage")
    public ResponseEntity<?> getUserSkillCoverage(@PathVariable UUID userId) {
        SkillCoverageReport coverage = progressService.getUserSkillCoverage(userId);
        return ResponseEntity.ok(coverage);
    }

    @GetMapping("/roadmap/{roadmapId}")
    public ResponseEntity<?> getRoadmapProgress(
            @PathVariable UUID roadmapId,
            @RequestParam UUID userId) {
        RoadmapProgress progress = progressService.getRoadmapProgress(userId, roadmapId);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/{topicId}/user/{userId}")
    public ResponseEntity<?> getTopicProgress(
            @PathVariable UUID topicId,
            @PathVariable UUID userId) {
        return userTopicProgressRepository.findByUserIdAndTopicId(userId, topicId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
