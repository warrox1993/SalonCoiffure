package com.jb.afrostyle.notifications.domain.entity;

import com.jb.afrostyle.notifications.domain.enums.NotificationChannel;
import com.jb.afrostyle.notifications.domain.enums.NotificationStatus;
import com.jb.afrostyle.notifications.domain.enums.NotificationType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "notification_queue")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false)
    private NotificationChannel channel;

    @Column(name = "recipient", nullable = false)
    private String recipient; // Email ou numéro de téléphone

    @Column(name = "subject")
    private String subject;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "scheduled_for", nullable = false)
    private LocalDateTime scheduledFor;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private NotificationStatus status = NotificationStatus.PENDING;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Column(name = "max_retries")
    private Integer maxRetries = 3;

    // Métadonnées additionnelles (JSON-like storage)
    @ElementCollection
    @CollectionTable(
        name = "notification_metadata",
        joinColumns = @JoinColumn(name = "notification_id")
    )
    @MapKeyColumn(name = "meta_key")
    @Column(name = "meta_value")
    private Map<String, String> metadata = new HashMap<>();

    // Références aux entités liées
    @Column(name = "booking_id")
    private Long bookingId;

    @Column(name = "availability_id")
    private Long availabilityId;

    // Constructeurs utilitaires
    public NotificationQueue(Long userId, NotificationType type, NotificationChannel channel, 
                           String recipient, String subject, String content, LocalDateTime scheduledFor) {
        this.userId = userId;
        this.type = type;
        this.channel = channel;
        this.recipient = recipient;
        this.subject = subject;
        this.content = content;
        this.scheduledFor = scheduledFor;
        this.status = NotificationStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    // Méthodes utilitaires
    public boolean isReady() {
        return status == NotificationStatus.PENDING && 
               LocalDateTime.now().isAfter(scheduledFor);
    }

    public boolean canRetry() {
        return retryCount < maxRetries && status == NotificationStatus.FAILED;
    }

    public void markAsSent() {
        this.status = NotificationStatus.SENT;
        this.sentAt = LocalDateTime.now();
    }

    public void markAsFailed(String errorMessage) {
        this.status = NotificationStatus.FAILED;
        this.errorMessage = errorMessage;
        this.retryCount++;
    }

    public void markAsProcessing() {
        this.status = NotificationStatus.PROCESSING;
    }

    public void addMetadata(String key, String value) {
        this.metadata.put(key, value);
    }

    public String getMetadata(String key) {
        return this.metadata.get(key);
    }

    // Pour les notifications immédiates
    public static NotificationQueue immediate(Long userId, NotificationType type, 
                                            NotificationChannel channel, String recipient, 
                                            String subject, String content) {
        return new NotificationQueue(userId, type, channel, recipient, subject, content, LocalDateTime.now());
    }

    // Pour les notifications programmées
    public static NotificationQueue scheduled(Long userId, NotificationType type, 
                                            NotificationChannel channel, String recipient, 
                                            String subject, String content, LocalDateTime when) {
        return new NotificationQueue(userId, type, channel, recipient, subject, content, when);
    }
}