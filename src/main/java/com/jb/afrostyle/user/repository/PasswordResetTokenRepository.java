package com.jb.afrostyle.user.repository;

import com.jb.afrostyle.user.domain.entity.PasswordResetToken;
import com.jb.afrostyle.user.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour la gestion des tokens de réinitialisation de mot de passe
 * Implémente les opérations CRUD et les requêtes de sécurité
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /**
     * Trouve un token de réinitialisation par sa valeur
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * Trouve tous les tokens valides d'un utilisateur
     */
    @Query("SELECT prt FROM PasswordResetToken prt WHERE prt.user = :user AND prt.isUsed = false AND prt.expiresAt > :now")
    List<PasswordResetToken> findValidTokensByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * Trouve tous les tokens d'un utilisateur (valides et expirés)
     */
    List<PasswordResetToken> findByUserOrderByCreatedAtDesc(User user);

    /**
     * Marque tous les tokens d'un utilisateur comme utilisés
     * Utilisé quand un token est utilisé avec succès
     */
    @Modifying
    @Query("UPDATE PasswordResetToken prt SET prt.isUsed = true WHERE prt.user = :user")
    void markAllUserTokensAsUsed(@Param("user") User user);

    /**
     * Supprime tous les tokens expirés
     * Tâche de maintenance à exécuter périodiquement
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken prt WHERE prt.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Supprime tous les tokens utilisés plus anciens que X jours
     * Pour nettoyer les anciens tokens
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken prt WHERE prt.isUsed = true AND prt.usedAt < :cutoffDate")
    void deleteOldUsedTokens(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Compte le nombre de tokens créés par un utilisateur dans une période donnée
     * Pour prévenir les attaques par déni de service
     */
    @Query("SELECT COUNT(prt) FROM PasswordResetToken prt WHERE prt.user = :user AND prt.createdAt > :since")
    Long countTokensByUserSince(@Param("user") User user, @Param("since") LocalDateTime since);

    /**
     * Trouve les tokens créés depuis une IP spécifique
     * Pour analyse de sécurité
     */
    @Query("SELECT prt FROM PasswordResetToken prt WHERE prt.ipAddress = :ipAddress AND prt.createdAt > :since")
    List<PasswordResetToken> findTokensByIpSince(@Param("ipAddress") String ipAddress, @Param("since") LocalDateTime since);

    /**
     * Vérifie si un token existe et est valide
     */
    @Query("SELECT CASE WHEN COUNT(prt) > 0 THEN true ELSE false END FROM PasswordResetToken prt WHERE prt.token = :token AND prt.isUsed = false AND prt.expiresAt > :now")
    boolean isTokenValid(@Param("token") String token, @Param("now") LocalDateTime now);

    /**
     * Trouve le token le plus récent d'un utilisateur
     */
    @Query("SELECT prt FROM PasswordResetToken prt WHERE prt.user = :user ORDER BY prt.createdAt DESC")
    List<PasswordResetToken> findLatestTokensByUser(@Param("user") User user);
}