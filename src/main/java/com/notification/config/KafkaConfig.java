package com.notification.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    // our main topic name
    public static final String NOTIFICATION_TOPIC =
            "notification-events";

    @Bean
    public NewTopic notificationTopic() {
        return TopicBuilder
                .name(NOTIFICATION_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }
}