package com.example.chatbot.learning;

import com.example.chatbot.ai.AIService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AssessmentService {

    private final AIService aiService;
    private final ProgressService progressService;
    private final TopicRepository topicRepository;
    private final ObjectMapper objectMapper;

    /**
     * Generate cross-questions for a topic to assess user knowledge
     */
    public AssessmentQuestion generateCrossQuestion(String topic, String userContext) {
        String prompt = String.format(
                "Generate a technical assessment question for '%s'. " +
                "User context: %s. " +
                "Provide a JSON response with: 'question' (the question text), 'difficulty' (EASY/MEDIUM/HARD), " +
                "'expectedKeywords' (list of key terms for answer validation), 'explanation' (detailed explanation). " +
                "Example: {\"question\": \"...\", \"difficulty\": \"MEDIUM\", \"expectedKeywords\": [\"...\"], \"explanation\": \"...\"}",
                topic, userContext
        );

        String aiResponse = aiService.generateReply(List.of(), prompt);
        aiResponse = cleanJsonMarkdown(aiResponse);

        try {
            Map<String, Object> questionData = objectMapper.readValue(aiResponse, new TypeReference<Map<String, Object>>() {});
            AssessmentQuestion aq = new AssessmentQuestion();
            aq.setQuestion((String) questionData.get("question"));
            aq.setDifficulty((String) questionData.getOrDefault("difficulty", "MEDIUM"));
            aq.setExpectedKeywords((List<String>) questionData.getOrDefault("expectedKeywords", List.of()));
            aq.setExplanation((String) questionData.get("explanation"));
            return aq;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse assessment question from AI", e);
        }
    }

    /**
     * Evaluate user's answer to a question and return score
     */
    public AssessmentResult evaluateAnswer(String question, String expectedKeywords, String userAnswer) throws JsonProcessingException {
        List<String> keywords = objectMapper.readValue(expectedKeywords, new TypeReference<List<String>>() {});

        String prompt = String.format(
                "Evaluate this answer: '%s' against the question: '%s'. " +
                "Expected keywords to cover: %s. " +
                "Return JSON with: 'score' (0-100), 'feedback' (brief feedback), 'isCorrect' (true/false). " +
                "Example: {\"score\": 85, \"feedback\": \"Good explanation...\", \"isCorrect\": true}",
                userAnswer, question, keywords
        );

        String aiResponse = aiService.generateReply(List.of(), prompt);
        aiResponse = cleanJsonMarkdown(aiResponse);

        try {
            Map<String, Object> result = objectMapper.readValue(aiResponse, new TypeReference<Map<String, Object>>() {});
            AssessmentResult ar = new AssessmentResult();
            ar.setScore(((Number) result.get("score")).intValue());
            ar.setFeedback((String) result.get("feedback"));
            ar.setCorrect((Boolean) result.get("isCorrect"));
            return ar;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse assessment result from AI", e);
        }
    }

    /**
     * Execute a full assessment quiz for a topic
     */
    @Transactional
    public AssessmentResult completeTopicAssessment(
            java.util.UUID userId,
            java.util.UUID topicId,
            List<AssessmentQuestion> questions,
            List<String> userAnswers) throws JsonProcessingException {

        if (questions.size() != userAnswers.size()) {
            throw new IllegalArgumentException("Question and answer counts must match");
        }

        int totalScore = 0;
        for (int i = 0; i < questions.size(); i++) {
            AssessmentQuestion q = questions.get(i);
            String expectedKeywords = objectMapper.writeValueAsString(q.getExpectedKeywords());
            AssessmentResult result = evaluateAnswer(q.getQuestion(), expectedKeywords, userAnswers.get(i));
            totalScore += result.getScore();
        }

        double averageScore = (double) totalScore / questions.size();

        // Update user's proficiency for this topic
        progressService.updateTopicProficiency(userId, topicId, averageScore, "Completed assessment");

        AssessmentResult finalResult = new AssessmentResult();
        finalResult.setScore((int) averageScore);
        finalResult.setCorrect(averageScore >= 70.0);
        finalResult.setFeedback("Assessment complete. Score: " + (int) averageScore + "%");
        return finalResult;
    }

    // Inner helper classes (can be moved to separate files if needed)
    public static class AssessmentQuestion {
        public String question;
        public String difficulty;
        public List<String> expectedKeywords;
        public String explanation;

        // Getters and setters
        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }
        public String getDifficulty() { return difficulty; }
        public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
        public List<String> getExpectedKeywords() { return expectedKeywords; }
        public void setExpectedKeywords(List<String> expectedKeywords) { this.expectedKeywords = expectedKeywords; }
        public String getExplanation() { return explanation; }
        public void setExplanation(String explanation) { this.explanation = explanation; }
    }

    public static class AssessmentResult {
        public int score;
        public String feedback;
        public boolean isCorrect;

        // Getters and setters
        public int getScore() { return score; }
        public void setScore(int score) { this.score = score; }
        public String getFeedback() { return feedback; }
        public void setFeedback(String feedback) { this.feedback = feedback; }
        public boolean isCorrect() { return isCorrect; }
        public void setCorrect(boolean correct) { isCorrect = correct; }
    }

    private String cleanJsonMarkdown(String json) {
        if (json == null) return null;
        json = json.trim();
        if (json.startsWith("```json")) {
            json = json.substring(7);
        }
        if (json.startsWith("```")) {
            json = json.substring(3);
        }
        if (json.endsWith("```")) {
            json = json.substring(0, json.length() - 3);
        }
        return json.trim();
    }
}
