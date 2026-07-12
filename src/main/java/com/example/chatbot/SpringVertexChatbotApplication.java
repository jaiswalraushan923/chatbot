package com.example.chatbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SpringVertexChatbotApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringVertexChatbotApplication.class, args);
    }
}
