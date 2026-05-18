package com.notification.controller;

import com.notification.dto.NotificationResponse;
import com.notification.dto.PublishEventRequest;
import com.notification.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService
            notificationService;

    public NotificationController(
            NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // publish event — Swiggy calls this
    @PostMapping("/publish")
    public ResponseEntity<?> publish(
            @RequestBody PublishEventRequest request) {
        notificationService.publishEvent(request);
        return ResponseEntity.ok(
                "Event published successfully!");
    }

    // get all notifications for user
    @GetMapping("/{userId}")
    public ResponseEntity<List<NotificationResponse>>
    getNotifications(
            @PathVariable String userId) {
        return ResponseEntity.ok(
                notificationService
                        .getNotifications(userId));
    }

    // get unread notifications
    @GetMapping("/{userId}/unread")
    public ResponseEntity<List<NotificationResponse>>
    getUnread(
            @PathVariable String userId) {
        return ResponseEntity.ok(
                notificationService.getUnread(userId));
    }

    // get unread count
    @GetMapping("/{userId}/unread/count")
    public ResponseEntity<Long> getUnreadCount(
            @PathVariable String userId) {
        return ResponseEntity.ok(
                notificationService.getUnreadCount(userId));
    }

    // mark notification as read
    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable String id) {
        return ResponseEntity.ok(
                notificationService.markAsRead(id));
    }
}