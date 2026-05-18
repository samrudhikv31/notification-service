package com.notification.model.postgresql;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "api_key",
            nullable = false,
            unique = true)
    private String apiKey;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user",
            cascade = CascadeType.ALL)
    private List<Subscription> subscriptions;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}