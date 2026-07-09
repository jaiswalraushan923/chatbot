package com.example.chatbot.session;


import com.example.chatbot.ai.AIService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
public class ChatController {

    private final SessionRepository sessionRepository;
    private final MessageRepository messageRepository;
    private final AIService aiService;

    @PostMapping("/{sessionId}/messages")
    public Message sendMessage(@PathVariable UUID sessionId,
                               @RequestBody String messageText) {

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow();

        Message userMessage = Message.builder()
                .session(session)
                .role(Role.USER)
                .content(messageText)
                .build();

        messageRepository.save(userMessage);

        List<Message> history =
                messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);

        String aiReply = aiService.generateReply(history, messageText);

        Message aiMessage = Message.builder()
                .session(session)
                .role(Role.AI)
                .content(aiReply)
                .build();

        return messageRepository.save(aiMessage);
    }

    @GetMapping("/{sessionId}/messages")
    public List<Message> getMessages(@PathVariable UUID sessionId) {
        return messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }
}

