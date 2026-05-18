package com.notification.websocket;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificationWebSocketHandler
        extends TextWebSocketHandler {

    // stores userId -> WebSocketSession
    // ConcurrentHashMap is thread safe
    private final ConcurrentHashMap<String, WebSocketSession>
            userSessions = new ConcurrentHashMap<>();

    private final RedisTemplate<String, String> redisTemplate;

    public NotificationWebSocketHandler(
            RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // called when user connects
    @Override
    public void afterConnectionEstablished(
            WebSocketSession session) throws Exception {

        // get userId from URL query param
        // ws://localhost:8081/ws/notifications?userId=user123
        String userId = getUserIdFromSession(session);

        if (userId != null) {
            // store session
            userSessions.put(userId, session);

            // mark user as online in Redis
            redisTemplate.opsForValue()
                    .set("online:" + userId,
                            session.getId());

            System.out.println(
                    "User connected: " + userId
                            + " | Total online: "
                            + userSessions.size());

            session.sendMessage(new TextMessage(
                    "{\"type\":\"CONNECTED\"," +
                            "\"message\":\"Connected successfully!\"}"));
        }
    }

    // called when user disconnects
    @Override
    public void afterConnectionClosed(
            WebSocketSession session,
            CloseStatus status) throws Exception {

        String userId = getUserIdFromSession(session);

        if (userId != null) {
            userSessions.remove(userId);

            // remove from Redis
            redisTemplate.delete("online:" + userId);

            System.out.println(
                    "User disconnected: " + userId
                            + " | Total online: "
                            + userSessions.size());
        }
    }

    // called when user sends a message
    @Override
    protected void handleTextMessage(
            WebSocketSession session,
            TextMessage message) throws Exception {

        // for now just echo back
        session.sendMessage(new TextMessage(
                "{\"type\":\"ECHO\",\"message\":\""
                        + message.getPayload() + "\"}"));
    }

    // send notification to specific user
    public void sendNotification(
            String userId,
            String notification) {

        WebSocketSession session =
                userSessions.get(userId);

        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(
                        new TextMessage(notification));
                System.out.println(
                        "Sent notification via WebSocket to: "
                                + userId);
            } catch (Exception e) {
                System.err.println(
                        "Failed to send WebSocket message: "
                                + e.getMessage());
            }
        } else {
            System.out.println(
                    "User " + userId
                            + " is offline, notification saved to MongoDB");
        }
    }

    // check if user is online
    public boolean isUserOnline(String userId) {
        return userSessions.containsKey(userId)
                && userSessions.get(userId).isOpen();
    }

    // helper to get userId from WebSocket URL
    private String getUserIdFromSession(
            WebSocketSession session) {
        String query = session.getUri() != null
                ? session.getUri().getQuery()
                : null;

        if (query != null && query.contains("userId=")) {
            return query.split("userId=")[1]
                    .split("&")[0];
        }
        return null;
    }
}