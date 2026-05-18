package com.notification.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification.config.KafkaConfig;
import com.notification.dto.NotificationEvent;
import com.notification.model.mongodb.Notification;
import com.notification.repository.mongodb.NotificationRepository;
import com.notification.websocket.NotificationWebSocketHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.HashMap;

@Component
public class NotificationConsumer {

    private final NotificationRepository
            notificationRepository;
    private final ObjectMapper objectMapper;
    private final NotificationWebSocketHandler
            webSocketHandler;

    public NotificationConsumer(
            NotificationRepository notificationRepository,
            ObjectMapper objectMapper,
            NotificationWebSocketHandler webSocketHandler) {
        this.notificationRepository = notificationRepository;
        this.objectMapper = objectMapper;
        this.webSocketHandler = webSocketHandler;
    }

    @KafkaListener(
            topics = KafkaConfig.NOTIFICATION_TOPIC,
            groupId = "notification-group"
    )
    public void consume(String message) {
        try {
            System.out.println(
                    "Received from Kafka: " + message);

            NotificationEvent event = objectMapper
                    .readValue(message,
                            NotificationEvent.class);

            // save to MongoDB
            Notification notification = Notification
                    .builder()
                    .userId(event.getUserId())
                    .type(event.getType())
                    .title(event.getTitle())
                    .message(event.getMessage())
                    .data(event.getData() != null
                            ? event.getData()
                            : new HashMap<>())
                    .isRead(false)
                    .deliveredViaWebSocket(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            Notification saved =
                    notificationRepository.save(notification);

            System.out.println(
                    "Saved to MongoDB for user: "
                            + event.getUserId());

            // try to send via WebSocket if user online
            if (webSocketHandler
                    .isUserOnline(event.getUserId())) {

                String wsMessage = objectMapper
                        .writeValueAsString(saved);

                webSocketHandler.sendNotification(
                        event.getUserId(), wsMessage);

                // update delivered flag in MongoDB
                saved.setDeliveredViaWebSocket(true);
                notificationRepository.save(saved);

            } else {
                System.out.println(
                        "User offline, will see on next login: "
                                + event.getUserId());
            }

        } catch (Exception e) {
            System.err.println(
                    "Error consuming message: "
                            + e.getMessage());
        }
    }
}