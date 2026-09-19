package com.example.chatbot.learning;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/assessments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AssessmentController {

    private final AssessmentService assessmentService;
    private final ProgressService progressService;

    @PostMapping("/generate-question")
    public ResponseEntity<?> generateQuestion(
            @RequestBody Map<String, String> request) {
        String topic = request.get("topic");
        String userContext = request.getOrDefault("userContext", "");

        AssessmentService.AssessmentQuestion question = assessmentService.generateCrossQuestion(topic, userContext);
        return ResponseEntity.ok(question);
    }

    @PostMapping("/evaluate-answer")
    public ResponseEntity<?> evaluateAnswer(
            @RequestBody Map<String, Object> request) {
        try {
            String question = (String) request.get("question");
            String expectedKeywords = (String) request.get("expectedKeywords");
            String userAnswer = (String) request.get("userAnswer");

            AssessmentService.AssessmentResult result = assessmentService.evaluateAnswer(question, expectedKeywords, userAnswer);
            return ResponseEntity.ok(result);
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to evaluate answer: " + e.getMessage()));
        }
    }

    @PostMapping("/complete-assessment")
    public ResponseEntity<?> completeAssessment(
            @RequestParam UUID userId,
            @RequestParam UUID topicId,
            @RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> questionsData = (List<Map<String, Object>>) request.get("questions");
            @SuppressWarnings("unchecked")
            List<String> userAnswers = (List<String>) request.get("answers");

            List<AssessmentService.AssessmentQuestion> questions = questionsData.stream()
                    .map(qData -> {
                        AssessmentService.AssessmentQuestion q = new AssessmentService.AssessmentQuestion();
                        q.setQuestion((String) qData.get("question"));
                        q.setDifficulty((String) qData.get("difficulty"));
                        @SuppressWarnings("unchecked")
                        List<String> keywords = (List<String>) qData.get("expectedKeywords");
                        q.setExpectedKeywords(keywords);
                        q.setExplanation((String) qData.get("explanation"));
                        return q;
                    })
                    .toList();

            AssessmentService.AssessmentResult result = assessmentService.completeTopicAssessment(userId, topicId, questions, userAnswers);
            return ResponseEntity.ok(result);
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Assessment error: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/recommended-topics")
    public ResponseEntity<?> getRecommendedTopics(
            @RequestParam UUID roadmapId,
            @RequestParam UUID userId) {
        var topics = progressService.getRecommendedNextTopics(roadmapId, userId);
        return ResponseEntity.ok(topics);
    }
}
