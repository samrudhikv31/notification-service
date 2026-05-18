package com.notification.dto;

import lombok.Data;
import java.util.Map;

@Data
public class PublishEventRequest {
    private String apiKey;
    private String userId;
    private String type;
    private String title;
    private String message;
    private Map<String, Object> data;
}