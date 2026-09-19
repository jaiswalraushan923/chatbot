package com.example.chatbot.learning;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProjectController {

    private final ProjectSubmissionRepository projectSubmissionRepository;
    private final com.example.chatbot.user.UserRepository userRepository;

    @PostMapping("/submit")
    public ResponseEntity<?> submitProject(
            @RequestParam UUID userId,
            @RequestBody Map<String, String> request) {
        String title = request.get("title");
        String description = request.get("description");
        String link = request.get("link");

        com.example.chatbot.user.User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        ProjectSubmission submission = ProjectSubmission.builder()
                .user(user)
                .title(title)
                .description(description)
                .link(link)
                .build();

        ProjectSubmission saved = projectSubmissionRepository.save(submission);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PostMapping("/{projectId}/assess")
    public ResponseEntity<?> assessProject(
            @PathVariable UUID projectId,
            @RequestBody Map<String, Object> request) {
        ProjectSubmission project = projectSubmissionRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        Integer score = (Integer) request.get("score");
        String notes = (String) request.get("notes");

        project.setAssessmentScore(score);
        project.setAssessmentNotes(notes);

        ProjectSubmission updated = projectSubmissionRepository.save(project);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserProjects(@PathVariable UUID userId) {
        // This requires a method in repository - we'll add it
        return ResponseEntity.ok(Map.of("message", "User projects endpoint"));
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<?> getProject(@PathVariable UUID projectId) {
        return projectSubmissionRepository.findById(projectId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<?> deleteProject(@PathVariable UUID projectId) {
        projectSubmissionRepository.deleteById(projectId);
        return ResponseEntity.ok(Map.of("message", "Project deleted"));
    }
}
