package com.notification.service;

import com.notification.dto.RegisterUserRequest;
import com.notification.dto.RegisterUserResponse;
import com.notification.model.postgresql.User;
import com.notification.repository.postgresql.UserRepository;
import com.notification.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(
                userRepository,
                jwtUtil
        );
    }

    @Test
    void shouldRegisterUserSuccessfully() {

        when(userRepository.existsByEmail(
                "dev@swiggy.com")).thenReturn(false);

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User u = invocation.getArgument(0);
                    u.setId(1L);
                    return u;
                });

        when(jwtUtil.generateToken(
                anyString(), anyString()))
                .thenReturn("mock-jwt-token");

        RegisterUserRequest request =
                new RegisterUserRequest();
        request.setName("Swiggy");
        request.setEmail("dev@swiggy.com");
        request.setPassword("password123");

        RegisterUserResponse response =
                userService.register(request);

        assertNotNull(response);
        assertEquals("Swiggy", response.getName());
        assertEquals("dev@swiggy.com",
                response.getEmail());
        assertNotNull(response.getApiKey());
        assertEquals("mock-jwt-token",
                response.getJwtToken());
    }

    @Test
    void shouldThrowWhenEmailAlreadyExists() {

        when(userRepository.existsByEmail(
                "dev@swiggy.com")).thenReturn(true);

        RegisterUserRequest request =
                new RegisterUserRequest();
        request.setName("Swiggy");
        request.setEmail("dev@swiggy.com");
        request.setPassword("password123");

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.register(request));

        assertEquals("Email already registered",
                exception.getMessage());
    }

    @Test
    void shouldGenerateUniqueApiKey() {

        when(userRepository.existsByEmail(anyString()))
                .thenReturn(false);
        when(userRepository.save(any(User.class)))
                .thenAnswer(i -> i.getArgument(0));
        when(jwtUtil.generateToken(
                anyString(), anyString()))
                .thenReturn("mock-token");

        RegisterUserRequest r1 =
                new RegisterUserRequest();
        r1.setName("Swiggy");
        r1.setEmail("swiggy@test.com");
        r1.setPassword("pass");

        RegisterUserRequest r2 =
                new RegisterUserRequest();
        r2.setName("Zomato");
        r2.setEmail("zomato@test.com");
        r2.setPassword("pass");

        RegisterUserResponse res1 =
                userService.register(r1);
        RegisterUserResponse res2 =
                userService.register(r2);

        // api keys should be different
        assertNotEquals(
                res1.getApiKey(),
                res2.getApiKey());
    }
}