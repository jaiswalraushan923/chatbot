package com.example.chatbot.auth;

import com.example.chatbot.user.User;
import com.example.chatbot.user.UserRepository;
import com.example.chatbot.user.UserService;
import com.example.chatbot.user.dto.AuthResponse;
import com.example.chatbot.user.dto.RegisterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {

        userService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("message", "User registered successfully"));
    }
//    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
//
//        User user = userService.register(request);
//
//        String token = jwtService.generateToken(user.getEmail());
//
//        return AuthResponse.builder()
//                .token(token)
//                .build();
//    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody RegisterRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtService.generateToken(user.getEmail());

       return AuthResponse.builder()
        .token(token)
        .userId(user.getId())
        .email(user.getEmail())
        .fullName(user.getFullName())
        .build();
    }
}
