package com.example.chatbot.learning;

import com.example.chatbot.user.User;
import com.example.chatbot.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private final UserTopicProgressRepository userTopicProgressRepository;
    private final TopicRepository topicRepository;
    private final RoadmapRepository roadmapRepository;
    private final RoadmapTopicRepository roadmapTopicRepository;
    private final ProjectSubmissionRepository projectSubmissionRepository;
    private final UserRepository userRepository;

    /**
     * Update user's proficiency for a topic based on assessment
     */

    @Transactional
    public ProgressResponse updateTopicProficiency(
            UUID userId,
            UUID topicId,
            double proficiency,
            String notes) {

        RoadmapTopic roadmapTopic = roadmapTopicRepository.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Roadmap topic not found"));

        Topic topic = roadmapTopic.getTopic();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        UserTopicProgress progress =
                userTopicProgressRepository
                        .findByUserIdAndTopicId(userId, topic.getId())
                        .orElseGet(() -> UserTopicProgress.builder()
                                .user(user)
                                .topic(topic)
                                .proficiency(0.0)
                                .build());

        progress.setUser(user);
        progress.setTopic(topic);
        progress.setProficiency(
                Math.min(100.0, Math.max(0.0, proficiency))
        );
        progress.setLastAssessedAt(LocalDateTime.now());
        progress.setNotes(notes);

        UserTopicProgress saved =
                userTopicProgressRepository.save(progress);

        return new ProgressResponse(
                saved.getId(),
                topic.getId(),
                saved.getProficiency(),
                saved.getLastAssessedAt(),
                saved.getNotes()
        );

    }


    /**
     * Get user's overall progress for a roadmap (coverage %)
     */

    public RoadmapProgress getRoadmapProgress(UUID userId, UUID roadmapId) {

        Roadmap roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new IllegalArgumentException("Roadmap not found"));

        List<RoadmapTopic> roadmapTopics = roadmapTopicRepository
                .findByRoadmapIdOrderByOrdering(roadmapId)
                .stream()
                .filter(RoadmapTopic::isIsanable)
                .toList();

        if (roadmapTopics.isEmpty()) {
            return RoadmapProgress.builder()
                    .roadmapId(roadmapId)
                    .totalTopics(0)
                    .completedTopics(0)
                    .overallCoveragePercent(0.0)
                    .averageProficiency(0.0)
                    .build();
        }

        // Get all progress of the user
        List<UserTopicProgress> userProgresses =
                userTopicProgressRepository.findByUserId(userId);

        // Get only topic IDs belonging to this roadmap
        Set<UUID> roadmapTopicIds = roadmapTopics.stream()
                .map(rt -> rt.getTopic().getId())
                .collect(Collectors.toSet());

        // Keep only progress for topics in this roadmap
        List<UserTopicProgress> roadmapProgresses = userProgresses.stream()
                .filter(p -> roadmapTopicIds.contains(p.getTopic().getId()))
                .toList();

        // Map roadmap topic progress by topic ID
        Map<UUID, UserTopicProgress> progressMap = roadmapProgresses.stream()
                .collect(Collectors.toMap(
                        p -> p.getTopic().getId(),
                        p -> p
                ));

        double totalProficiency = 0;
        int completedTopics = 0;

        for (RoadmapTopic rt : roadmapTopics) {

            UserTopicProgress prog = progressMap.get(rt.getTopic().getId());

            if (prog != null) {
                totalProficiency += prog.getProficiency();

                if (prog.getProficiency() >= 70.0) {
                    completedTopics++;
                }
            }
        }

        double overallCoverage =
                ((double) completedTopics / roadmapTopics.size()) * 100;

        double avgProficiency =
                totalProficiency / roadmapTopics.size();

        return RoadmapProgress.builder()
                .roadmapId(roadmapId)
                .totalTopics(roadmapTopics.size())
                .completedTopics(completedTopics)
                .overallCoveragePercent(overallCoverage)
                .averageProficiency(avgProficiency)
                .topicProgresses(
                        roadmapProgresses.stream()
                                .map(p -> TopicProgressDTO.builder()
                                        .topicId(p.getTopic().getId())
                                        .topicTitle(p.getTopic().getTitle())
                                        .proficiency(p.getProficiency())
                                        .lastAssessedAt(p.getLastAssessedAt())
                                        .build())
                                .collect(Collectors.toList())
                )
                .build();
    }


    /**
     * Get user's overall skill coverage across all topics
     */
    public SkillCoverageReport getUserSkillCoverage(UUID userId) {
        List<UserTopicProgress> allProgresses = userTopicProgressRepository.findByUserId(userId);

        if (allProgresses.isEmpty()) {
            return SkillCoverageReport.builder()
                    .userId(userId)
                    .totalTopicsTracked(0)
                    .masteredTopics(0)
                    .proficientTopics(0)
                    .beginnerTopics(0)
                    .overallSkillPercentage(0.0)
                    .build();
        }

        int mastered = (int) allProgresses.stream()
                .filter(p -> p.getProficiency() >= 80.0)
                .count();
        int proficient = (int) allProgresses.stream()
                .filter(p -> p.getProficiency() >= 50.0 && p.getProficiency() < 80.0)
                .count();
        int beginner = (int) allProgresses.stream()
                .filter(p -> p.getProficiency() < 50.0)
                .count();

        double overallSkill = allProgresses.stream()
                .mapToDouble(UserTopicProgress::getProficiency)
                .average()
                .orElse(0.0);

        return SkillCoverageReport.builder()
                .userId(userId)
                .totalTopicsTracked(allProgresses.size())
                .masteredTopics(mastered)
                .proficientTopics(proficient)
                .beginnerTopics(beginner)
                .overallSkillPercentage(overallSkill)
                .build();
    }

    /**
     * Get next recommended topics for learning based on current progress
     */
    public List<Topic> getRecommendedNextTopics(UUID roadmapId, UUID userId) {
        List<RoadmapTopic> roadmapTopics = roadmapTopicRepository.findByRoadmapIdOrderByOrdering(roadmapId)
                .stream()
                .filter(RoadmapTopic::isIsanable)
                .toList();
        List<UserTopicProgress> userProgresses = userTopicProgressRepository.findByUserId(userId);

        Map<UUID, Double> progressMap = userProgresses.stream()
                .collect(Collectors.toMap(p -> p.getTopic().getId(), UserTopicProgress::getProficiency));

        return roadmapTopics.stream()
                .filter(rt -> {
                    Double proficiency = progressMap.getOrDefault(rt.getTopic().getId(), 0.0);
                    return proficiency < 70.0;
                })
                .sorted(Comparator.comparingInt(RoadmapTopic::getOrdering))
                .limit(5)
                .map(RoadmapTopic::getTopic)
                .collect(Collectors.toList());
    }

    /**
     * Update progress based on project submission assessment
     */
    @Transactional
    public void updateProgressFromProjectSubmission(UUID projectSubmissionId, Integer assessmentScore, String assessmentNotes) {
        // This will be connected to ProjectSubmission later
        // For now, just a placeholder for the logic flow
    }
}
