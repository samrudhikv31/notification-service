package com.notification.repository.postgresql;

import com.notification.model.postgresql.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SubscriptionRepository
        extends JpaRepository<Subscription, Long> {

    List<Subscription> findByUserIdAndIsActive(
            Long userId, Boolean isActive);

    boolean existsByUserIdAndEventTypeAndIsActive(
            Long userId, String eventType, Boolean isActive);
}