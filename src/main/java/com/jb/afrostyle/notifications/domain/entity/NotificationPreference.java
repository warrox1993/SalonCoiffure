package com.jb.afrostyle.notifications.domain.entity;

import com.jb.afrostyle.notifications.domain.enums.NotificationType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "notification_preferences")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "email_enabled", nullable = false)
    private boolean emailEnabled = true;

    @Column(name = "sms_enabled", nullable = false)
    private boolean smsEnabled = false;

    @Column(name = "web_push_enabled", nullable = false)
    private boolean webPushEnabled = true;

    @Column(name = "email_address")
    private String emailAddress;

    @Column(name = "phone_number")
    private String phoneNumber;

    @ElementCollection(targetClass = NotificationType.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(
        name = "notification_subscriptions",
        joinColumns = @JoinColumn(name = "preference_id")
    )
    @Column(name = "notification_type")
    private Set<NotificationType> subscribedEvents = new HashSet<>();

    // Constructeur pour un utilisateur avec préférences par défaut
    public NotificationPreference(Long userId, String emailAddress, String phoneNumber) {
        this.userId = userId;
        this.emailAddress = emailAddress;
        this.phoneNumber = phoneNumber;
        this.emailEnabled = true;
        this.smsEnabled = false;
        this.webPushEnabled = true;
        
        // Abonnements par défaut
        this.subscribedEvents.add(NotificationType.BOOKING_CONFIRMATION);
        this.subscribedEvents.add(NotificationType.BOOKING_REMINDER);
        this.subscribedEvents.add(NotificationType.NEW_AVAILABILITY);
    }

    // Méthodes utilitaires
    public boolean isSubscribedTo(NotificationType type) {
        return subscribedEvents.contains(type);
    }

    public void subscribe(NotificationType type) {
        subscribedEvents.add(type);
    }

    public void unsubscribe(NotificationType type) {
        subscribedEvents.remove(type);
    }

    public boolean canReceiveEmail() {
        return emailEnabled && emailAddress != null && !emailAddress.trim().isEmpty();
    }

    public boolean canReceiveSms() {
        return smsEnabled && phoneNumber != null && !phoneNumber.trim().isEmpty();
    }
}