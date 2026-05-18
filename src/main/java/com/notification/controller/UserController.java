package com.notification.controller;

import com.notification.dto.RegisterUserRequest;
import com.notification.dto.RegisterUserResponse;
import com.notification.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok(
                "Notification Service is running!");
    }
    // Register user
    @PostMapping("/register")
    public ResponseEntity<RegisterUserResponse> register(
            @RequestBody RegisterUserRequest request) {
        RegisterUserResponse response =
                userService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}