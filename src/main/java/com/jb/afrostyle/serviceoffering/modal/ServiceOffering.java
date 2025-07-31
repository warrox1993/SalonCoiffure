package com.jb.afrostyle.serviceoffering.modal;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Service offert par le salon unique.
 * 
 * MODÈLE MONO-SALON :
 * - Plus de salonId (tous les services appartiennent au salon unique)
 * - Services globaux pour l'application
 * - Simplification de la logique métier
 * 
 * MIGRATION MULTI-SALON → MONO-SALON :
 * - Suppression salonId (champ obligatoire avant)
 * - Suppression relations JPA vers Salon
 * - Plus de validation ownership
 * - Amélioration type price (int → BigDecimal)
 * 
 * @author AfroStyle Team
 * @since 2.0 (Migration mono-salon)
 */
@Entity
@Data
@Table(name = "service_offerings", indexes = {
    @Index(name = "idx_service_offerings_name", columnList = "name"),
    @Index(name = "idx_service_offerings_price", columnList = "price"),
    @Index(name = "idx_service_offerings_duration", columnList = "duration")
})
public class ServiceOffering {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "Le nom du service est obligatoire")
    private String name;

    @Column(nullable = false, length = 1000)
    @NotBlank(message = "La description du service est obligatoire")
    private String description;

    /**
     * Prix du service en euros (avec centimes).
     * Changement int → BigDecimal pour précision monétaire.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Le prix du service est obligatoire")
    @DecimalMin(value = "0.01", message = "Le prix doit être supérieur à 0")
    @DecimalMax(value = "9999.99", message = "Le prix ne peut pas dépasser 9999.99€")
    private BigDecimal price;

    /**
     * Durée du service en minutes.
     */
    @Column(nullable = false)
    @NotNull(message = "La durée du service est obligatoire")
    @Min(value = 5, message = "La durée minimale est de 5 minutes")
    @Max(value = 600, message = "La durée maximale est de 600 minutes (10h)")
    private Integer duration;


    /**
     * URLs des images du service (séparées par des virgules).
     */
    @Column(length = 1000)
    private String images;

    /**
     * Indique si le service est actif et peut être réservé.
     */
    @Column(nullable = false)
    private Boolean active = true;

    /**
     * Ordre d'affichage du service dans sa catégorie.
     */
    private Integer displayOrder = 0;

    /**
     * Tags pour recherche et filtrage.
     */
    @Column(length = 500)
    private String tags;

    /**
     * Date de création du service.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Date de dernière mise à jour du service.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // =========================
    // MÉTHODES UTILITAIRES
    // =========================

    /**
     * Retourne le prix formaté en euros.
     * 
     * @return Prix formaté (ex: "25.50€")
     */
    public String getFormattedPrice() {
        return price != null ? String.format("%.2f€", price) : "0.00€";
    }

    /**
     * Retourne la durée formatée en heures et minutes.
     * 
     * @return Durée formatée (ex: "1h 30min" ou "45min")
     */
    public String getFormattedDuration() {
        if (duration == null) return "0min";
        
        int hours = duration / 60;
        int minutes = duration % 60;
        
        if (hours > 0 && minutes > 0) {
            return String.format("%dh %02dmin", hours, minutes);
        } else if (hours > 0) {
            return String.format("%dh", hours);
        } else {
            return String.format("%dmin", minutes);
        }
    }
}