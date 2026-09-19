package com.example.chatbot.learning;

import com.example.chatbot.ai.AIService;
import com.example.chatbot.learning.dto.AiTopicDto;
import com.example.chatbot.learning.dto.RoadmapResponse;
import com.example.chatbot.learning.dto.TopicResponse;
import com.example.chatbot.learning.mapper.RoadmapMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoadmapService {

    private final RoadmapRepository roadmapRepository;
    private final GoalRepository goalRepository;
    private final TopicRepository topicRepository;
    private final RoadmapTopicRepository roadmapTopicRepository;
    private final AIService aiService;
    private final ObjectMapper objectMapper;
    private final RoadmapMapper roadmapMapper;

    @Async
    @Transactional
    public void generateRoadmapAsync(UUID goalId) throws JsonProcessingException {
        generateRoadmap(goalId);
    }

    @Transactional
    public Roadmap generateRoadmap(UUID goalId) throws JsonProcessingException {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found"));

        // AI generates a list of topic slugs based on the goal
        String aiPrompt = """
You are an expert software engineering mentor.

Create a COMPLETE learning roadmap for the career goal:

"%s"

Rules:

1. Return ONLY valid JSON.
2. No markdown.
3. No explanation.
4. Output must be a JSON array.
5. Every topic can contain children.
6. Include ALL important concepts.
7. Generate a roadmap similar in depth to roadmap.sh.
8. Every leaf topic should represent a single concept someone can study.
9. Maximum depth: 4 levels.

JSON format:

[
  {
    "title": "Topic Name",
    "difficulty": "BEGINNER",
    "estimatedHours": 30,
    "children": [
      {
        "title": "Sub Topic",
        "difficulty": "BEGINNER",
        "estimatedHours": 10,
        "children": [
          {
            "title": "Nested Topic",
            "difficulty": "BEGINNER",
            "estimatedHours": 5,
            "children": []
          }
        ]
      }
    ]
  }
]

The roadmap should be exhaustive and professionally structured.
""".formatted(goal.getName());

//        String aiPrompt = String.format(
//                "For the career goal '%s', provide a structured learning path as a JSON array of topic objects. " +
//                "Each object should have: 'slug' (unique-identifier-like), 'title' (readable name), 'description' (brief explanation). " +
//                "Provide topics in learning order. Format: [{\"slug\": \"topic_slug\", \"title\": \"Topic Title\", \"description\": \"...\"}, ...]",
//                goal.getName()
//        );

        System.out.println("1. Goal found");

        String aiResponse = aiService.generateReply(List.of(), aiPrompt);
        System.out.println("2. AI Response:");
        System.out.println(aiResponse);

        List<AiTopicDto> topics = parseTopicsFromAI(aiResponse);
        System.out.println("3. Parsed topics = " + topics.size());

        Roadmap roadmap = Roadmap.builder()
                .goal(goal)
                .name(goal.getName() + " Roadmap")
                .build();

        roadmap = roadmapRepository.save(roadmap);
        System.out.println("4. Roadmap saved");

        int[] orderingArray = { 0 };
        AtomicInteger ordering = new AtomicInteger(0);

        saveTopics(
                roadmap,
                null,          // root topics have no parent
                topics
        );
        return roadmap;
    }

    private String generateSlug(String title) {
        return title.toLowerCase()
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_|_$", "");
    }


    private void saveTopics(
            Roadmap roadmap,
            RoadmapTopic parent,
            List<AiTopicDto> topics) {

        for (int i = 0; i < topics.size(); i++) {

            AiTopicDto dto = topics.get(i);

            String slug = generateSlug(dto.getTitle());

            Topic topic = topicRepository.findBySlug(slug)
                    .orElseGet(() ->
                            topicRepository.save(
                                    Topic.builder()
                                            .slug(slug)
                                            .title(dto.getTitle())
                                            .difficulty(dto.getDifficulty())
                                            .estimatedHours(dto.getEstimatedHours())
                                            .build()
                            ));

            RoadmapTopic roadmapTopic =
                    roadmapTopicRepository.save(

                            RoadmapTopic.builder()
                                    .roadmap(roadmap)
                                    .topic(topic)
                                    .parent(parent)
                                    .ordering(i)
                                    .build()

                    );

            if(dto.getChildren()!=null){

                saveTopics(
                        roadmap,
                        roadmapTopic,
                        dto.getChildren()
                );

            }

        }

    }



    private List<AiTopicDto> parseTopicsFromAI(String aiResponse) throws JsonProcessingException {

        // Remove markdown if Gemini returns ```json ... ```
        aiResponse = aiResponse.trim();

        if (aiResponse.startsWith("```json")) {
            aiResponse = aiResponse.substring(7);
        }

        if (aiResponse.startsWith("```")) {
            aiResponse = aiResponse.substring(3);
        }

        if (aiResponse.endsWith("```")) {
            aiResponse = aiResponse.substring(0, aiResponse.length() - 3);
        }

        aiResponse = aiResponse.trim();

//        return objectMapper.readValue(
//                aiResponse,
//                new TypeReference<List<Map<String, Object>>>() {}
//        );

        List<AiTopicDto> topics =
                objectMapper.readValue(
                        aiResponse,
                        new TypeReference<List<AiTopicDto>>() {});

        return topics;
    }

//    public Roadmap getRoadmap(UUID roadmapId) {
//        return roadmapRepository.findById(roadmapId)
//                .orElseThrow(() -> new IllegalArgumentException("Roadmap not found"));
//    }

    public RoadmapResponse getRoadmap(UUID roadmapId) {

        Roadmap roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new IllegalArgumentException("Roadmap not found"));

        return roadmapMapper.toResponse(roadmap);
    }

    public Roadmap getRoadmapEntity(UUID roadmapId) {
        return roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new IllegalArgumentException("Roadmap not found"));
    }

    @Transactional
    public void addTopicToRoadmap(UUID roadmapId, UUID topicId, UUID parentId, Integer ordering, String notes) {
        Roadmap roadmap = getRoadmapEntity(roadmapId);
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found"));

        RoadmapTopic parent = null;
        if (parentId != null) {
            parent = roadmapTopicRepository.findById(parentId)
                    .orElseThrow(() -> new IllegalArgumentException("Parent RoadmapTopic not found"));
            if (!parent.getRoadmap().getId().equals(roadmapId)) {
                throw new IllegalArgumentException("Parent topic does not belong to the specified roadmap");
            }
        }

        List<RoadmapTopic> siblings;
        if (parent == null) {
            siblings = roadmapTopicRepository.findByRoadmapIdAndParentIsNullOrderByOrdering(roadmapId);
        } else {
            siblings = roadmapTopicRepository.findByRoadmapIdAndParentIdOrderByOrdering(roadmapId, parentId);
        }

        int finalOrdering;
        if (ordering == null) {
            finalOrdering = siblings.size();
        } else {
            finalOrdering = Math.max(0, Math.min(ordering, siblings.size()));
        }

        for (RoadmapTopic sibling : siblings) {
            if (sibling.getOrdering() >= finalOrdering) {
                sibling.setOrdering(sibling.getOrdering() + 1);
                roadmapTopicRepository.save(sibling);
            }
        }

        RoadmapTopic roadmapTopic = RoadmapTopic.builder()
                .roadmap(roadmap)
                .topic(topic)
                .parent(parent)
                .ordering(finalOrdering)
                .notes(notes)
                .isanable(true)
                .build();
        roadmapTopicRepository.save(roadmapTopic);
    }

    @Transactional
    public void removeTopicFromRoadmap(UUID roadmapTopicId) {
        RoadmapTopic roadmapTopic = roadmapTopicRepository.findById(roadmapTopicId)
                .orElseThrow(() -> new IllegalArgumentException("RoadmapTopic not found"));
        disableRoadmapTopicRecursive(roadmapTopic);
    }

    private void disableRoadmapTopicRecursive(RoadmapTopic roadmapTopic) {
        roadmapTopic.setIsanable(false);
        roadmapTopicRepository.save(roadmapTopic);
        if (roadmapTopic.getChildren() != null) {
            for (RoadmapTopic child : roadmapTopic.getChildren()) {
                disableRoadmapTopicRecursive(child);
            }
        }
    }

    public RoadmapResponse getRoadmapByGoalId(UUID goalId) {

        Roadmap roadmap = roadmapRepository.findByGoalId(goalId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Roadmap not found"));

        return roadmapMapper.toResponse(roadmap);
    }
}
