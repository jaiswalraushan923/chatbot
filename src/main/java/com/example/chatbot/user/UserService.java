package com.example.chatbot.user;


import com.example.chatbot.user.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
//            throw new RuntimeException("Email already exists");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email already exists"
            );
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .careerStage(request.getCareerStage())
                .build();

        return userRepository.save(user);
    }
}
