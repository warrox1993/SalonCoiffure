package com.jb.afrostyle.notifications.repository;

import com.jb.afrostyle.notifications.domain.enums.NotificationChannel;
import com.jb.afrostyle.notifications.domain.enums.NotificationStatus;
import com.jb.afrostyle.notifications.domain.enums.NotificationType;
import com.jb.afrostyle.notifications.domain.entity.NotificationQueue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationQueueRepository extends JpaRepository<NotificationQueue, Long> {

    // Trouver les notifications prêtes à être envoyées
    @Query("SELECT nq FROM NotificationQueue nq " +
           "WHERE nq.status = :status AND nq.scheduledFor <= :now")
    List<NotificationQueue> findReadyToSend(@Param("status") NotificationStatus status, 
                                           @Param("now") LocalDateTime now);

    // Notifications en échec qui peuvent être retentées
    @Query("SELECT nq FROM NotificationQueue nq " +
           "WHERE nq.status = :status AND nq.retryCount < nq.maxRetries")
    List<NotificationQueue> findFailedRetryable(@Param("status") NotificationStatus status);

    // Notifications par utilisateur
    Page<NotificationQueue> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // Notifications par canal
    List<NotificationQueue> findByChannelAndStatus(NotificationChannel channel, NotificationStatus status);

    // Notifications par type et statut
    List<NotificationQueue> findByTypeAndStatus(NotificationType type, NotificationStatus status);

    // Nettoyer les anciennes notifications
    @Modifying
    @Query("DELETE FROM NotificationQueue nq " +
           "WHERE nq.status IN (:finalStatuses) AND nq.createdAt < :cutoffDate")
    int deleteOldNotifications(@Param("finalStatuses") List<NotificationStatus> finalStatuses,
                              @Param("cutoffDate") LocalDateTime cutoffDate);

    // Statistiques
    @Query("SELECT nq.status, COUNT(nq) FROM NotificationQueue nq " +
           "WHERE nq.createdAt >= :since GROUP BY nq.status")
    List<Object[]> getNotificationStats(@Param("since") LocalDateTime since);

    @Query("SELECT nq.channel, COUNT(nq) FROM NotificationQueue nq " +
           "WHERE nq.createdAt >= :since AND nq.status = :status " +
           "GROUP BY nq.channel")
    List<Object[]> getChannelStats(@Param("since") LocalDateTime since, 
                                  @Param("status") NotificationStatus status);

    // Notifications en cours de traitement depuis trop longtemps (pour détecter les blocages)
    @Query("SELECT nq FROM NotificationQueue nq " +
           "WHERE nq.status = :status AND nq.createdAt < :stuckThreshold")
    List<NotificationQueue> findStuckNotifications(@Param("status") NotificationStatus status,
                                                  @Param("stuckThreshold") LocalDateTime stuckThreshold);

    // Compter les notifications en attente pour un utilisateur
    long countByUserIdAndStatus(Long userId, NotificationStatus status);

    // Trouver les notifications liées à une réservation
    List<NotificationQueue> findByBookingId(Long bookingId);
    
    // Nouvelles méthodes pour le nettoyage et les statistiques
    
    /**
     * Supprime les notifications envoyées plus anciennes que la date spécifiée
     */
    @Modifying
    @Query("DELETE FROM NotificationQueue nq WHERE nq.status = 'SENT' AND nq.sentAt < :cutoffDate")
    int deleteOldSentNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Supprime les notifications échouées plus anciennes que la date spécifiée
     */
    @Modifying
    @Query("DELETE FROM NotificationQueue nq WHERE nq.status = 'FAILED' AND nq.createdAt < :cutoffDate")
    int deleteOldFailedNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Supprime les notifications annulées plus anciennes que la date spécifiée
     */
    @Modifying
    @Query("DELETE FROM NotificationQueue nq WHERE nq.status = 'CANCELLED' AND nq.createdAt < :cutoffDate")
    int deleteOldCancelledNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Compte les notifications créées depuis une date donnée
     */
    @Query("SELECT COUNT(nq) FROM NotificationQueue nq WHERE nq.createdAt >= :since")
    long countNotificationsSince(@Param("since") LocalDateTime since);
    
    /**
     * Compte les notifications en attente
     */
    @Query("SELECT COUNT(nq) FROM NotificationQueue nq WHERE nq.status = 'PENDING'")
    long countPendingNotifications();
    
    /**
     * Compte les notifications en cours de traitement
     */
    @Query("SELECT COUNT(nq) FROM NotificationQueue nq WHERE nq.status = 'PROCESSING'")
    long countProcessingNotifications();
    
    /**
     * Compte les notifications envoyées depuis une date donnée
     */
    @Query("SELECT COUNT(nq) FROM NotificationQueue nq WHERE nq.status = 'SENT' AND nq.sentAt >= :since")
    long countSentNotificationsSince(@Param("since") LocalDateTime since);
    
    /**
     * Compte les notifications échouées depuis une date donnée
     */
    @Query("SELECT COUNT(nq) FROM NotificationQueue nq WHERE nq.status = 'FAILED' AND nq.createdAt >= :since")
    long countFailedNotificationsSince(@Param("since") LocalDateTime since);
    
    /**
     * Compte les notifications par canal depuis une date donnée
     */
    @Query("SELECT COUNT(nq) FROM NotificationQueue nq WHERE nq.channel = :channel AND nq.createdAt >= :since")
    long countNotificationsByChannelSince(@Param("channel") String channel, @Param("since") LocalDateTime since);
    
    /**
     * Compte les notifications par type depuis une date donnée
     */
    @Query("SELECT COUNT(nq) FROM NotificationQueue nq WHERE nq.type = :type AND nq.createdAt >= :since")
    long countNotificationsByTypeSince(@Param("type") String type, @Param("since") LocalDateTime since);
    
    /**
     * Marque les notifications qui ont dépassé le nombre maximum de tentatives comme échouées
     */
    @Modifying
    @Query("UPDATE NotificationQueue nq SET nq.status = 'FAILED' WHERE nq.retryCount >= :maxRetries AND nq.status = 'PROCESSING'")
    int markExceededRetriesAsFailed(@Param("maxRetries") int maxRetries);
}