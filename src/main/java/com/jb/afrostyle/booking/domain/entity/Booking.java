package com.jb.afrostyle.booking.domain.entity;

import com.jb.afrostyle.booking.domain.enums.BookingStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Réservation pour le salon unique.
 * 
 * MODÈLE MONO-SALON :
 * - Plus de salonId (toutes les réservations pour le salon unique)
 * - Simplification de la logique de réservation
 * - Plus de validation ownership salon
 * 
 * MIGRATION MULTI-SALON → MONO-SALON :
 * - Suppression salonId (champ obligatoire avant)
 * - Suppression champ salonName @Transient (plus utile)
 * - Amélioration type totalPrice (int → BigDecimal)
 * - Nettoyage getters/setters redondants avec Lombok
 * 
 * @author AfroStyle Team
 * @since 2.0 (Migration mono-salon)
 */
@Entity
@Data
@Table(name = "booking", indexes = {
    @Index(name = "idx_bookings_customer_id", columnList = "customerId"),
    @Index(name = "idx_bookings_start_time", columnList = "startTime"),
    @Index(name = "idx_bookings_status", columnList = "status"),
    @Index(name = "idx_bookings_booking_date", columnList = "bookingDate")
})
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * ID du client qui a fait la réservation.
     */
    @NotNull(message = "L'ID du client est obligatoire")
    private Long customerId;

    /**
     * ID du service principal (pour compatibilité ascendante).
     * @deprecated Utiliser serviceIds à la place
     */
    @Deprecated
    private Long serviceId;

    /**
     * Heure de début de la réservation.
     */
    @NotNull(message = "L'heure de début est obligatoire")
    @Future(message = "La réservation doit être dans le futur")
    private LocalDateTime startTime;

    /**
     * Heure de fin de la réservation.
     */
    @NotNull(message = "L'heure de fin est obligatoire")
    private LocalDateTime endTime;

    /**
     * Liste des IDs des services réservés.
     */
    @ElementCollection
    @CollectionTable(name = "booking_service_ids", joinColumns = @JoinColumn(name = "booking_id"))
    @Column(name = "service_id")
    private Set<Long> serviceIds;

    /**
     * Statut de la réservation.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.PENDING;

    /**
     * Prix total de la réservation en euros (avec centimes).
     * Changement int → BigDecimal pour précision monétaire.
     */
    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Le prix total est obligatoire")
    @DecimalMin(value = "0.01", message = "Le prix total doit être supérieur à 0")
    private BigDecimal totalPrice = BigDecimal.ZERO;

    /**
     * Nombre total de services dans la réservation.
     */
    @Column(name = "total_services", nullable = false)
    private Integer totalServices = 1;

    /**
     * Date/heure de création de la réservation.
     */
    private LocalDateTime bookingDate;

    /**
     * Notes additionnelles du client.
     */
    @Column(length = 1000)
    private String notes;

    /**
     * ID de l'événement Google Calendar associé.
     */
    @Column(name = "google_calendar_event_id")
    private String googleCalendarEventId;

    // =========================
    // CHAMPS TRANSIENTS POUR AFFICHAGE
    // =========================

    /**
     * Nom du client (calculé, pas persisté).
     */
    @Transient
    private String userName;

    /**
     * Nom du service principal (calculé, pas persisté).
     */
    @Transient
    private String serviceName;

    /**
     * Nom du salon (toujours "AfroStyle Salon" pour le salon unique).
     */
    @Transient
    private String salonName;

    // =========================
    // MÉTADONNÉES
    // =========================

    /**
     * Date de création de la réservation.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Date de dernière mise à jour de la réservation.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // =========================
    // MÉTHODES UTILITAIRES
    // =========================

    /**
     * Retourne le prix total formaté en euros.
     * 
     * @return Prix formaté (ex: "45.50€")
     */
    public String getFormattedTotalPrice() {
        return totalPrice != null ? String.format("%.2f€", totalPrice) : "0.00€";
    }

    /**
     * Vérifie si la réservation est confirmée.
     * 
     * @return true si le statut est CONFIRMED
     */
    public boolean isConfirmed() {
        return BookingStatus.CONFIRMED.equals(status);
    }

    /**
     * Vérifie si la réservation est annulée.
     * 
     * @return true si le statut est CANCELLED
     */
    public boolean isCancelled() {
        return BookingStatus.CANCELLED.equals(status);
    }

    /**
     * Vérifie si la réservation peut être annulée.
     * 
     * @return true si le statut permet l'annulation
     */
    public boolean canBeCancelled() {
        return BookingStatus.PENDING.equals(status) || BookingStatus.CONFIRMED.equals(status);
    }
}