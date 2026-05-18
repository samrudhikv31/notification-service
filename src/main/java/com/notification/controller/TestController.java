package com.notification.controller;

import com.notification.dto.NotificationEvent;
import com.notification.kafka.NotificationProducer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private final NotificationProducer producer;

    public TestController(
            NotificationProducer producer) {
        this.producer = producer;
    }

    @PostMapping("/publish")
    public ResponseEntity<?> publish(
            @RequestBody NotificationEvent event) {
        producer.publish(event);
        return ResponseEntity.ok(
                "Event published to Kafka!");
    }
}