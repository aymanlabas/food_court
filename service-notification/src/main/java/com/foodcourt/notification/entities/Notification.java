package com.foodcourt.notification.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private Long orderId;
    private String recipient;
    @Enumerated(EnumType.STRING)
    private NotificationType type;
    @Column(nullable = false, length = 1000)
    private String message;
    @Builder.Default
    private boolean read = false;
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
