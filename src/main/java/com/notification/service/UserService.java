package com.notification.service;

import com.notification.dto.RegisterUserRequest;
import com.notification.dto.RegisterUserResponse;
import com.notification.model.postgresql.User;
import com.notification.repository.postgresql.UserRepository;
import com.notification.security.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public RegisterUserResponse register(
            RegisterUserRequest request) {

        if (userRepository.existsByEmail(
                request.getEmail())) {
            throw new RuntimeException(
                    "Email already registered");
        }

        String apiKey = UUID.randomUUID()
                .toString()
                .replace("-", "");

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(
                request.getPassword()));
        user.setApiKey(apiKey);
        user.setIsActive(true);

        userRepository.save(user);

        String token = jwtUtil.generateToken(
                user.getEmail(), apiKey);

        return RegisterUserResponse.builder()
                .name(user.getName())
                .email(user.getEmail())
                .apiKey(apiKey)
                .jwtToken(token)
                .message("User registered successfully! " +
                        "Save your API key and JWT token.")
                .build();
    }
}