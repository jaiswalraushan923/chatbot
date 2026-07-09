package com.example.chatbot.session;


import com.example.chatbot.user.User;
import com.example.chatbot.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    @PostMapping
    public Session createSession(@RequestParam String title) {

        String email = (String) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        User user = userRepository.findByEmail(email)
                .orElseThrow();

        Session session = Session.builder()
                .user(user)
                .title(title)
                .build();

        return sessionRepository.save(session);
    }

    @GetMapping
    public List<Session> getUserSessions() {

        String email = (String) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        User user = userRepository.findByEmail(email)
                .orElseThrow();

        return sessionRepository.findByUserId(user.getId());
    }

    @DeleteMapping("/{id}")
    public void deleteSession(@PathVariable UUID id) {

        sessionRepository.deleteById(id);
    }
}
