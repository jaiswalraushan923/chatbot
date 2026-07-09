package com.example.chatbot.ai;

import com.example.chatbot.session.Message;
import com.example.chatbot.session.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AIService {

    private final GeminiClient geminiClient;

    public String generateReply(List<Message> history, String userMessage) {

        StringBuilder prompt = new StringBuilder();

        prompt.append("You are Clarity, a career and communication strategist.\n\n");

        for (Message msg : history) {
            prompt.append(msg.getRole())
                    .append(": ")
                    .append(msg.getContent())
                    .append("\n");
        }

        prompt.append("USER: ").append(userMessage);

        return geminiClient.generate(prompt.toString());
    }
}

