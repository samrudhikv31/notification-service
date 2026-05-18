package com.notification.repository.mongodb;

import com.notification.model.mongodb.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository
        extends MongoRepository<Notification, String> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(
            String userId);

    List<Notification> findByUserIdAndIsRead(
            String userId, Boolean isRead);

    Long countByUserIdAndIsRead(
            String userId, Boolean isRead);
}