package com.notification.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification.config.KafkaConfig;
import com.notification.dto.NotificationEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class NotificationProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public NotificationProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publish(NotificationEvent event) {
        try {
            // convert event to JSON string
            String message = objectMapper
                    .writeValueAsString(event);

            // send to Kafka topic
            kafkaTemplate.send(
                    KafkaConfig.NOTIFICATION_TOPIC,
                    event.getUserId(),
                    message
            );

            System.out.println("Published event to Kafka: "
                    + event.getType()
                    + " for user: "
                    + event.getUserId());

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to publish event: "
                            + e.getMessage());
        }
    }
}