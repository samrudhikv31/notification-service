package com.notification.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(
        basePackages =
                "com.notification.repository.mongodb"
)
public class MongoConfig {
}