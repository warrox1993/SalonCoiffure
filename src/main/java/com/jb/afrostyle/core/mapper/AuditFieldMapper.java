package com.jb.afrostyle.core.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Utilitaires MapStruct pour les champs d'audit (createdAt, updatedAt, etc.)
 * Centralise la gestion des champs d'audit dans tous les mappers
 * Fournit des règles communes pour les timestamps et métadonnées
 * 
 * @version 1.0
 * @since Java 21
 */
@Mapper(componentModel = "spring")
@Component
public class AuditFieldMapper {
    
    // ==================== GESTION DES TIMESTAMPS ====================
    
    /**
     * Initialise createdAt avec la date/heure actuelle
     * Utilisé lors de la création d'entités
     * @return LocalDateTime actuel
     */
    @Named("initializeCreatedAt")
    public LocalDateTime initializeCreatedAt() {
        return LocalDateTime.now();
    }
    
    /**
     * Met à jour updatedAt avec la date/heure actuelle
     * Utilisé lors de la mise à jour d'entités
     * @return LocalDateTime actuel
     */
    @Named("updateUpdatedAt")
    public LocalDateTime updateUpdatedAt() {
        return LocalDateTime.now();
    }
    
    /**
     * Préserve createdAt existant (ne pas écraser)
     * Utilisé dans les mappings DTO->Entity pour préserver l'original
     * @param existing Valeur existante
     * @return Valeur existante inchangée
     */
    @Named("preserveCreatedAt")
    public LocalDateTime preserveCreatedAt(LocalDateTime existing) {
        return existing; // Toujours préserver la valeur existante
    }
    
    /**
     * Force la mise à jour de updatedAt même si une valeur existe
     * Utilisé pour s'assurer que updatedAt reflète toujours la dernière modification
     * @param existing Valeur existante (ignorée)
     * @return LocalDateTime actuel
     */
    @Named("forceUpdateUpdatedAt")
    public LocalDateTime forceUpdateUpdatedAt(LocalDateTime existing) {
        return LocalDateTime.now(); // Toujours utiliser maintenant
    }
    
    /**
     * Initialise createdAt seulement s'il est null
     * Utilisé pour les entités qui peuvent être partiellement créées
     * @param existing Valeur existante
     * @return Valeur existante ou maintenant si null
     */
    @Named("initializeCreatedAtIfNull")
    public LocalDateTime initializeCreatedAtIfNull(LocalDateTime existing) {
        return existing != null ? existing : LocalDateTime.now();
    }
    
    /**
     * Met à jour updatedAt seulement s'il est null
     * Utilisé pour les cas où updatedAt peut déjà être défini
     * @param existing Valeur existante
     * @return Valeur existante ou maintenant si null
     */
    @Named("updateUpdatedAtIfNull")
    public LocalDateTime updateUpdatedAtIfNull(LocalDateTime existing) {
        return existing != null ? existing : LocalDateTime.now();
    }
    
    // ==================== GESTION DES VERSIONS ====================
    
    /**
     * Initialise la version à 0 pour les nouvelles entités
     * @return Version initiale (0)
     */
    @Named("initializeVersion")
    public Integer initializeVersion() {
        return 0;
    }
    
    /**
     * Incrémente la version existante
     * @param currentVersion Version actuelle
     * @return Version incrémentée
     */
    @Named("incrementVersion")
    public Integer incrementVersion(Integer currentVersion) {
        return currentVersion != null ? currentVersion + 1 : 1;
    }
    
    /**
     * Préserve la version existante
     * @param existing Version existante
     * @return Version inchangée
     */
    @Named("preserveVersion")
    public Integer preserveVersion(Integer existing) {
        return existing;
    }
    
    // ==================== GESTION DES UTILISATEURS D'AUDIT ====================
    
    /**
     * Définit l'utilisateur de création (créé par)
     * Note: Dans une implémentation complète, ceci devrait utiliser SecurityContext
     * @param userId ID de l'utilisateur créateur
     * @return ID utilisateur
     */
    @Named("setCreatedBy")
    public Long setCreatedBy(Long userId) {
        return userId;
    }
    
    /**
     * Définit l'utilisateur de modification (modifié par)
     * @param userId ID de l'utilisateur modificateur
     * @return ID utilisateur
     */
    @Named("setUpdatedBy")
    public Long setUpdatedBy(Long userId) {
        return userId;
    }
    
    /**
     * Préserve l'utilisateur créateur existant
     * @param existing ID utilisateur créateur existant
     * @return ID utilisateur inchangé
     */
    @Named("preserveCreatedBy")
    public Long preserveCreatedBy(Long existing) {
        return existing;
    }
    
    // ==================== UTILITAIRES DE VALIDATION ====================
    
    /**
     * Vérifie si les champs d'audit sont cohérents
     * @param createdAt Date de création
     * @param updatedAt Date de modification
     * @return true si cohérent (updatedAt >= createdAt)
     */
    @Named("areAuditFieldsConsistent")
    public Boolean areAuditFieldsConsistent(LocalDateTime createdAt, LocalDateTime updatedAt) {
        if (createdAt == null || updatedAt == null) {
            return true; // Null est considéré comme valide
        }
        
        return !updatedAt.isBefore(createdAt);
    }
    
    /**
     * Calcule l'âge d'une entité en jours
     * @param createdAt Date de création
     * @return Âge en jours ou null
     */
    @Named("calculateAgeInDays")
    public Long calculateAgeInDays(LocalDateTime createdAt) {
        if (createdAt == null) {
            return null;
        }
        
        return java.time.Duration.between(createdAt, LocalDateTime.now()).toDays();
    }
    
    /**
     * Calcule le temps depuis la dernière modification en heures
     * @param updatedAt Date de dernière modification
     * @return Heures depuis modification ou null
     */
    @Named("calculateHoursSinceUpdate")
    public Long calculateHoursSinceUpdate(LocalDateTime updatedAt) {
        if (updatedAt == null) {
            return null;
        }
        
        return java.time.Duration.between(updatedAt, LocalDateTime.now()).toHours();
    }
    
    // ==================== FORMATAGE POUR AFFICHAGE ====================
    
    /**
     * Formate createdAt pour l'affichage utilisateur
     * @param createdAt Date de création
     * @return String formatée ou null
     */
    @Named("formatCreatedAtForDisplay")
    public String formatCreatedAtForDisplay(LocalDateTime createdAt) {
        if (createdAt == null) {
            return null;
        }
        
        return createdAt.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"));
    }
    
    /**
     * Formate updatedAt pour l'affichage utilisateur
     * @param updatedAt Date de modification
     * @return String formatée ou null
     */
    @Named("formatUpdatedAtForDisplay")
    public String formatUpdatedAtForDisplay(LocalDateTime updatedAt) {
        if (updatedAt == null) {
            return null;
        }
        
        return updatedAt.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"));
    }
    
    /**
     * Génère un texte relatif pour l'âge (ex: "il y a 2 heures")
     * @param timestamp Timestamp à analyser
     * @return Texte relatif ou null
     */
    @Named("formatRelativeTime")
    public String formatRelativeTime(LocalDateTime timestamp) {
        if (timestamp == null) {
            return null;
        }
        
        java.time.Duration duration = java.time.Duration.between(timestamp, LocalDateTime.now());
        
        long minutes = duration.toMinutes();
        long hours = duration.toHours();
        long days = duration.toDays();
        
        if (minutes < 1) {
            return "à l'instant";
        } else if (minutes < 60) {
            return String.format("il y a %d minute%s", minutes, minutes > 1 ? "s" : "");
        } else if (hours < 24) {
            return String.format("il y a %d heure%s", hours, hours > 1 ? "s" : "");
        } else if (days < 30) {
            return String.format("il y a %d jour%s", days, days > 1 ? "s" : "");
        } else {
            // Pour les dates anciennes, afficher la date complète
            return formatCreatedAtForDisplay(timestamp);
        }
    }
    
    // ==================== MÉTADONNÉES D'AUDIT ====================
    
    /**
     * Génère un résumé d'audit pour une entité
     * @param createdAt Date de création
     * @param updatedAt Date de modification
     * @param createdBy Créé par (ID utilisateur)
     * @param updatedBy Modifié par (ID utilisateur)
     * @return Résumé d'audit
     */
    @Named("generateAuditSummary")
    public String generateAuditSummary(LocalDateTime createdAt, LocalDateTime updatedAt, 
                                      Long createdBy, Long updatedBy) {
        StringBuilder summary = new StringBuilder();
        
        if (createdAt != null) {
            summary.append("Créé le ").append(formatCreatedAtForDisplay(createdAt));
            if (createdBy != null) {
                summary.append(" par l'utilisateur ").append(createdBy);
            }
        }
        
        if (updatedAt != null && !updatedAt.equals(createdAt)) {
            if (summary.length() > 0) {
                summary.append(", ");
            }
            summary.append("modifié le ").append(formatUpdatedAtForDisplay(updatedAt));
            if (updatedBy != null && !updatedBy.equals(createdBy)) {
                summary.append(" par l'utilisateur ").append(updatedBy);
            }
        }
        
        return summary.length() > 0 ? summary.toString() : null;
    }
    
    /**
     * Détermine si une entité a été modifiée depuis sa création
     * @param createdAt Date de création
     * @param updatedAt Date de modification
     * @return true si modifiée
     */
    @Named("hasBeenModified")
    public Boolean hasBeenModified(LocalDateTime createdAt, LocalDateTime updatedAt) {
        if (createdAt == null || updatedAt == null) {
            return false;
        }
        
        // Considérer comme modifié si plus de 1 seconde de différence
        return java.time.Duration.between(createdAt, updatedAt).toSeconds() > 1;
    }
    
    /**
     * Vérifie si une entité est récente (créée dans les dernières 24h)
     * @param createdAt Date de création
     * @return true si récente
     */
    @Named("isRecentEntity")
    public Boolean isRecentEntity(LocalDateTime createdAt) {
        if (createdAt == null) {
            return false;
        }
        
        return createdAt.isAfter(LocalDateTime.now().minusHours(24));
    }
    
    /**
     * Vérifie si une entité a été récemment modifiée (dans les dernières 2h)
     * @param updatedAt Date de modification
     * @return true si récemment modifiée
     */
    @Named("isRecentlyUpdated")
    public Boolean isRecentlyUpdated(LocalDateTime updatedAt) {
        if (updatedAt == null) {
            return false;
        }
        
        return updatedAt.isAfter(LocalDateTime.now().minusHours(2));
    }
}