package com.example.chatbot.learning;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/goals")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GoalController {

    private final GoalRepository goalRepository;
    private final RoadmapService roadmapService;

    @PostMapping("/create")
    public ResponseEntity<?> createGoal(
            @RequestParam UUID userId,
            @RequestBody Map<String, String> request) throws JsonProcessingException {
        String goalName = request.get("name");
        String description = request.get("description");

        com.example.chatbot.user.User user = new com.example.chatbot.user.User();
        user.setId(userId);

        Goal goal = Goal.builder()
                .user(user)
                .name(goalName)
                .description(description)
                .build();


        Goal savedGoal = goalRepository.save(goal);

        roadmapService.generateRoadmapAsync(savedGoal.getId());


        return ResponseEntity.status(HttpStatus.CREATED).body(savedGoal);
    }

    @GetMapping("/{goalId}")
    public ResponseEntity<?> getGoal(@PathVariable UUID goalId) {
        return goalRepository.findById(goalId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserGoals(@PathVariable UUID userId) {
        return ResponseEntity.ok(goalRepository.findByUserId(userId));
    }

    @DeleteMapping("/{goalId}")
    public ResponseEntity<?> deleteGoal(@PathVariable UUID goalId) {
        goalRepository.deleteById(goalId);
        return ResponseEntity.ok(Map.of("message", "Goal deleted successfully"));
    }
}
