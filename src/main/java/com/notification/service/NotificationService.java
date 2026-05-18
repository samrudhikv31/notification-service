package com.notification.service;

import com.notification.dto.NotificationEvent;
import com.notification.dto.NotificationResponse;
import com.notification.dto.PublishEventRequest;
import com.notification.kafka.NotificationProducer;
import com.notification.model.mongodb.Notification;
import com.notification.model.postgresql.User;
import com.notification.repository.mongodb.NotificationRepository;
import com.notification.repository.postgresql.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository
            notificationRepository;
    private final UserRepository userRepository;
    private final NotificationProducer producer;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            NotificationProducer producer) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.producer = producer;
    }

    // publish event to Kafka
    public void publishEvent(
            PublishEventRequest request) {

        // validate api key
        User user = userRepository
                .findByApiKey(request.getApiKey())
                .orElseThrow(() -> new RuntimeException(
                        "Invalid API key"));

        if (!user.getIsActive()) {
            throw new RuntimeException(
                    "User account is disabled");
        }

        // build and publish event
        NotificationEvent event = NotificationEvent
                .builder()
                .userId(request.getUserId())
                .type(request.getType())
                .title(request.getTitle())
                .message(request.getMessage())
                .data(request.getData())
                .build();

        producer.publish(event);
    }

    // get all notifications for a user
    public List<NotificationResponse> getNotifications(
            String userId) {

        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // get unread notifications
    public List<NotificationResponse> getUnread(
            String userId) {

        return notificationRepository
                .findByUserIdAndIsRead(userId, false)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // mark notification as read
    public NotificationResponse markAsRead(String id) {

        Notification notification = notificationRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Notification not found"));

        notification.setIsRead(true);
        notificationRepository.save(notification);

        return mapToResponse(notification);
    }

    // get unread count
    public Long getUnreadCount(String userId) {
        return notificationRepository
                .countByUserIdAndIsRead(userId, false);
    }

    // helper to map entity to response
    private NotificationResponse mapToResponse(
            Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .userId(n.getUserId())
                .type(n.getType())
                .title(n.getTitle())
                .message(n.getMessage())
                .data(n.getData())
                .isRead(n.getIsRead())
                .deliveredViaWebSocket(
                        n.getDeliveredViaWebSocket())
                .createdAt(n.getCreatedAt())
                .build();
    }
}