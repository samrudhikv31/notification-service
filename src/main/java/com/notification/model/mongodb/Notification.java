package com.notification.model.mongodb;

import lombok.Data;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "notifications")
@Data
@Builder
public class Notification {

    @Id
    private String id;

    private String userId;

    private String type;

    private String title;

    private String message;

    // flexible field — stores any extra data
    // order notification → orderId, amount
    // payment notification → txnId, bank
    // delivery notification → driverId, eta
    private Map<String, Object> data;

    private Boolean isRead = false;

    private Boolean deliveredViaWebSocket = false;

    private LocalDateTime createdAt;
}