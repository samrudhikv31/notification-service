package com.notification.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification.dto.NotificationEvent;
import com.notification.model.mongodb.Notification;
import com.notification.repository.mongodb.NotificationRepository;
import com.notification.websocket.NotificationWebSocketHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Map;
import static org.mockito.Mockito.*;

class NotificationConsumerTest {

    @Mock
    private NotificationRepository
            notificationRepository;

    @Mock
    private NotificationWebSocketHandler
            webSocketHandler;

    private NotificationConsumer consumer;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        consumer = new NotificationConsumer(
                notificationRepository,
                objectMapper,
                webSocketHandler
        );
    }

    @Test
    void shouldSaveNotificationToMongoDB()
            throws Exception {

        // create test event
        NotificationEvent event = NotificationEvent
                .builder()
                .userId("user123")
                .type("ORDER_PLACED")
                .title("Order Confirmed!")
                .message("Your order is confirmed")
                .data(Map.of("orderId", "ORD123"))
                .build();

        String message = objectMapper
                .writeValueAsString(event);

        // mock save returning notification
        Notification saved = Notification.builder()
                .id("notif1")
                .userId("user123")
                .type("ORDER_PLACED")
                .title("Order Confirmed!")
                .message("Your order is confirmed")
                .isRead(false)
                .deliveredViaWebSocket(false)
                .build();

        when(notificationRepository.save(any()))
                .thenReturn(saved);
        when(webSocketHandler.isUserOnline("user123"))
                .thenReturn(false);

        // consume the message
        consumer.consume(message);

        // verify saved to MongoDB
        verify(notificationRepository, times(1))
                .save(any(Notification.class));
    }

    @Test
    void shouldSendWebSocketWhenUserOnline()
            throws Exception {

        NotificationEvent event = NotificationEvent
                .builder()
                .userId("user123")
                .type("PAYMENT_SUCCESS")
                .title("Payment Done!")
                .message("Payment successful")
                .build();

        String message = objectMapper
                .writeValueAsString(event);

        Notification saved = Notification.builder()
                .id("notif1")
                .userId("user123")
                .type("PAYMENT_SUCCESS")
                .title("Payment Done!")
                .message("Payment successful")
                .isRead(false)
                .deliveredViaWebSocket(false)
                .build();

        when(notificationRepository.save(any()))
                .thenReturn(saved);

        // user is online
        when(webSocketHandler.isUserOnline("user123"))
                .thenReturn(true);

        consumer.consume(message);

        // verify WebSocket was called
        verify(webSocketHandler, times(1))
                .sendNotification(
                        eq("user123"), anyString());
    }

    @Test
    void shouldNotSendWebSocketWhenUserOffline()
            throws Exception {

        NotificationEvent event = NotificationEvent
                .builder()
                .userId("user123")
                .type("ORDER_PLACED")
                .title("Order Confirmed!")
                .message("Your order is confirmed")
                .build();

        String message = objectMapper
                .writeValueAsString(event);

        Notification saved = Notification.builder()
                .id("notif1")
                .userId("user123")
                .isRead(false)
                .deliveredViaWebSocket(false)
                .build();

        when(notificationRepository.save(any()))
                .thenReturn(saved);

        // user is offline
        when(webSocketHandler.isUserOnline("user123"))
                .thenReturn(false);

        consumer.consume(message);

        // verify WebSocket was NOT called
        verify(webSocketHandler, never())
                .sendNotification(anyString(), anyString());
    }

    @Test
    void shouldHandleInvalidMessageGracefully() {
        // send invalid JSON
        String badMessage = "this is not valid json!!!";

        // should not throw exception
        consumer.consume(badMessage);

        // verify nothing was saved
        verify(notificationRepository, never())
                .save(any());
    }
}