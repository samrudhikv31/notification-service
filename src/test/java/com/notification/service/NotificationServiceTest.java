package com.notification.service;

import com.notification.dto.NotificationResponse;
import com.notification.dto.PublishEventRequest;
import com.notification.kafka.NotificationProducer;
import com.notification.model.mongodb.Notification;
import com.notification.model.postgresql.User;
import com.notification.repository.mongodb.NotificationRepository;
import com.notification.repository.postgresql.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationServiceTest {

    @Mock
    private NotificationRepository
            notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationProducer producer;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        notificationService = new NotificationService(
                notificationRepository,
                userRepository,
                producer
        );
    }

    @Test
    void shouldPublishEventSuccessfully() {

        // mock valid user
        User user = new User();
        user.setId(1L);
        user.setApiKey("valid-api-key");
        user.setIsActive(true);

        when(userRepository.findByApiKey("valid-api-key"))
                .thenReturn(Optional.of(user));

        PublishEventRequest request =
                new PublishEventRequest();
        request.setApiKey("valid-api-key");
        request.setUserId("user123");
        request.setType("ORDER_PLACED");
        request.setTitle("Order Confirmed!");
        request.setMessage("Your order is confirmed");

        // should not throw
        assertDoesNotThrow(() ->
                notificationService.publishEvent(request));

        // verify producer was called
        verify(producer, times(1)).publish(any());
    }

    @Test
    void shouldThrowWhenApiKeyInvalid() {

        when(userRepository.findByApiKey("wrong-key"))
                .thenReturn(Optional.empty());

        PublishEventRequest request =
                new PublishEventRequest();
        request.setApiKey("wrong-key");
        request.setUserId("user123");
        request.setType("ORDER_PLACED");
        request.setTitle("Test");
        request.setMessage("Test message");

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> notificationService
                        .publishEvent(request));

        assertEquals("Invalid API key",
                exception.getMessage());
    }

    @Test
    void shouldReturnNotificationsForUser() {

        Notification n1 = Notification.builder()
                .id("1")
                .userId("user123")
                .type("ORDER_PLACED")
                .title("Order Confirmed!")
                .message("Your order is confirmed")
                .isRead(false)
                .deliveredViaWebSocket(true)
                .createdAt(LocalDateTime.now())
                .build();

        Notification n2 = Notification.builder()
                .id("2")
                .userId("user123")
                .type("PAYMENT_SUCCESS")
                .title("Payment Done!")
                .message("Payment successful")
                .isRead(true)
                .deliveredViaWebSocket(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(notificationRepository
                .findByUserIdOrderByCreatedAtDesc("user123"))
                .thenReturn(List.of(n1, n2));

        List<NotificationResponse> result =
                notificationService
                        .getNotifications("user123");

        assertEquals(2, result.size());
        assertEquals("ORDER_PLACED",
                result.get(0).getType());
        assertEquals("PAYMENT_SUCCESS",
                result.get(1).getType());
    }

    @Test
    void shouldReturnEmptyListWhenNoNotifications() {

        when(notificationRepository
                .findByUserIdOrderByCreatedAtDesc("user999"))
                .thenReturn(List.of());

        List<NotificationResponse> result =
                notificationService
                        .getNotifications("user999");

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldMarkNotificationAsRead() {

        Notification notification = Notification.builder()
                .id("notif1")
                .userId("user123")
                .type("ORDER_PLACED")
                .title("Order Confirmed!")
                .message("Your order is confirmed")
                .isRead(false)
                .deliveredViaWebSocket(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(notificationRepository.findById("notif1"))
                .thenReturn(Optional.of(notification));
        when(notificationRepository.save(any()))
                .thenReturn(notification);

        NotificationResponse result =
                notificationService.markAsRead("notif1");

        // verify isRead was set to true
        verify(notificationRepository, times(1))
                .save(argThat(n -> n.getIsRead() == true));
    }

    @Test
    void shouldThrowWhenNotificationNotFound() {

        when(notificationRepository.findById("wrong-id"))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> notificationService
                        .markAsRead("wrong-id"));

        assertEquals("Notification not found",
                exception.getMessage());
    }

    @Test
    void shouldReturnUnreadCount() {

        when(notificationRepository
                .countByUserIdAndIsRead("user123", false))
                .thenReturn(5L);

        Long count = notificationService
                .getUnreadCount("user123");

        assertEquals(5L, count);
    }
}