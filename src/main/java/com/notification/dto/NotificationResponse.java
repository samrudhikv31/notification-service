package com.notification.dto;

import lombok.Data;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class NotificationResponse {
    private String id;
    private String userId;
    private String type;
    private String title;
    private String message;
    private Map<String, Object> data;
    private Boolean isRead;
    private Boolean deliveredViaWebSocket;
    private LocalDateTime createdAt;
}