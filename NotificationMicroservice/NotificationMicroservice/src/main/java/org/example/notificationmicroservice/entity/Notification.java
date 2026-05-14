package org.example.notificationmicroservice.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.notificationmicroservice.enums.NotificationType;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipient_mail")
    private String recipientMail;

    @Column(name = "recipient_phone")
    private String recipientPhone;

    @Column(name = "recipient_username", nullable = false)
    private String recipientUsername;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false)
    private String subject;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private NotificationType notificationType;

    @Column(name="is_sent")
    private Boolean isSent = false;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

}
