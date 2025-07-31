package com.jb.afrostyle.salon.modal;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Configuration du salon unique de l'application.
 * 
 * MODÈLE MONO-SALON :
 * - Un seul salon par application (id fixe = 1)
 * - Plus de notion de propriétaire (ownerId supprimé)
 * - Configuration centralisée du salon
 * - Toutes les réservations, services, catégories se réfèrent à ce salon unique
 * 
 * MIGRATION MULTI-SALON → MONO-SALON :
 * - Suppression ownerId (plus de multi-propriétaires)
 * - ID fixe à 1 (un seul enregistrement)
 * - Configuration simplifiée
 * 
 * @author AfroStyle Team
 * @since 2.0 (Migration mono-salon)
 */
@Entity
@Data
@Table(name = "salon_settings", indexes = {
    @Index(name = "idx_salon_settings_name", columnList = "name"),
    @Index(name = "idx_salon_settings_city", columnList = "city"),
    @Index(name = "idx_salon_settings_address", columnList = "address")
})
public class SalonSettings {

    /**
     * ID fixe = 1 pour le salon unique.
     * Il n'y aura jamais qu'un seul salon dans l'application.
     */
    @Id
    private Long id = 1L;

    /**
     * Nom du salon.
     */
    @Column(nullable = false)
    @NotBlank(message = "Le nom du salon est obligatoire")
    private String name;

    /**
     * Adresse complète du salon.
     */
    @Column(nullable = false)
    @NotBlank(message = "L'adresse du salon est obligatoire")
    private String address;

    /**
     * Ville du salon.
     */
    @Column(nullable = false)
    @NotBlank(message = "La ville du salon est obligatoire")
    private String city;

    /**
     * Téléphone du salon.
     */
    @Column(nullable = false)
    @NotBlank(message = "Le téléphone du salon est obligatoire")
    private String phone;

    /**
     * Email de contact du salon.
     */
    @Column(nullable = false)
    @Email(message = "L'email doit être valide")
    @NotBlank(message = "L'email du salon est obligatoire")
    private String email;

    /**
     * Heure d'ouverture du salon.
     */
    @Column(nullable = false)
    @NotNull(message = "L'heure d'ouverture est obligatoire")
    private LocalTime openTime;

    /**
     * Heure de fermeture du salon.
     */
    @Column(nullable = false)
    @NotNull(message = "L'heure de fermeture est obligatoire")
    private LocalTime closeTime;

    /**
     * Images du salon (URLs ou chemins).
     */
    @ElementCollection
    @CollectionTable(name = "salon_images", joinColumns = @JoinColumn(name = "salon_id"))
    @Column(name = "image_url")
    private List<String> images;

    // =========================
    // COORDONNÉES GPS - GOOGLE MAPS
    // =========================

    /**
     * Latitude GPS du salon pour géolocalisation.
     */
    @Column(name = "latitude")
    private Double latitude;

    /**
     * Longitude GPS du salon pour géolocalisation.
     */
    @Column(name = "longitude")
    private Double longitude;

    /**
     * ID Google Place pour intégration Google Maps.
     */
    @Column(name = "google_place_id")
    private String googlePlaceId;

    /**
     * Adresse formatée par Google Maps.
     */
    @Column(name = "formatted_address", length = 500)
    private String formattedAddress;

    // =========================
    // MÉTADONNÉES
    // =========================

    /**
     * Date de création de la configuration.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Date de dernière mise à jour de la configuration.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // =========================
    // MÉTHODES UTILITAIRES
    // =========================

    /**
     * Vérifie si le salon est géolocalisé.
     * 
     * @return true si latitude et longitude sont définies
     */
    public boolean isGeolocated() {
        return latitude != null && longitude != null;
    }

    /**
     * Retourne l'adresse complète formatée.
     * 
     * @return adresse complète
     */
    public String getFullAddress() {
        return formattedAddress != null && !formattedAddress.isEmpty() 
            ? formattedAddress 
            : address + ", " + city;
    }

    /**
     * Vérifie si le salon est ouvert à une heure donnée.
     * 
     * @param time l'heure à vérifier
     * @return true si le salon est ouvert à cette heure
     */
    public boolean isOpenAt(LocalTime time) {
        if (openTime == null || closeTime == null || time == null) {
            return false;
        }
        
        // Gestion des horaires qui traversent minuit (ex: 09:00-02:00)
        if (openTime.isBefore(closeTime)) {
            return !time.isBefore(openTime) && !time.isAfter(closeTime);
        } else {
            return !time.isBefore(openTime) || !time.isAfter(closeTime);
        }
    }
}