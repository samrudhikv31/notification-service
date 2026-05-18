package com.notification.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(
        basePackages =
                "com.notification.repository.postgresql"
)
@EntityScan(
        basePackages =
                "com.notification.model.postgresql"
)
public class PostgresConfig {
}