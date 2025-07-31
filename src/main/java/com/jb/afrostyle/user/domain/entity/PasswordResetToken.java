package com.jb.afrostyle.user.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entité pour gérer les tokens de réinitialisation de mot de passe
 * Implémente les meilleures pratiques de sécurité :
 * - Tokens à usage unique
 * - Expiration automatique
 * - Limitation du nombre de tentatives
 */
@Entity
@Table(name = "password_reset_tokens", indexes = {
        @Index(name = "idx_token", columnList = "token", unique = true),
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_expires_at", columnList = "expires_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Token de réinitialisation unique et sécurisé
     * Généré cryptographiquement sécurisé
     */
    @Column(name = "token", nullable = false, unique = true, length = 255)
    private String token;

    /**
     * Utilisateur propriétaire du token
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Date d'expiration du token
     * Les tokens de réinitialisation ont une durée de vie courte (15-30 minutes)
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Date de création du token
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Indique si le token a été utilisé
     * Un token utilisé ne peut plus être réutilisé (one-time use)
     */
    @Column(name = "is_used", nullable = false)
    private Boolean isUsed = false;

    /**
     * Adresse IP depuis laquelle le token a été demandé
     * Pour des vérifications de sécurité supplémentaires
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * User-Agent du client qui a demandé le token
     * Pour détecter des usages suspects
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * Date d'utilisation du token
     */
    @Column(name = "used_at")
    private LocalDateTime usedAt;

    /**
     * Constructeur pour création d'un nouveau token
     */
    public PasswordResetToken(String token, User user, LocalDateTime expiresAt, 
                             String ipAddress, String userAgent) {
        this.token = token;
        this.user = user;
        this.expiresAt = expiresAt;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.isUsed = false;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Vérifie si le token est encore valide
     */
    public boolean isValid() {
        return !isExpired() && !isUsed;
    }

    /**
     * Vérifie si le token est expiré
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Marque le token comme utilisé
     */
    public void markAsUsed() {
        this.isUsed = true;
        this.usedAt = LocalDateTime.now();
    }
}