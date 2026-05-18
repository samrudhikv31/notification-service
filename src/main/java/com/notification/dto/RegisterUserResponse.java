package com.notification.dto;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class RegisterUserResponse {
    private String name;
    private String email;
    private String apiKey;
    private String jwtToken;
    private String message;
}