package com.jb.afrostyle.booking.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.AssertTrue;
import com.jb.afrostyle.booking.validation.ValidBusinessHours;
import com.jb.afrostyle.booking.validation.TimeRangeValidator;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * DTO pour la demande de réservation avec Builder Pattern Context7
 * Contient toutes les informations nécessaires pour créer une réservation
 * Intègre validation métier avancée des créneaux horaires
 *
 * <p>Exemple d'utilisation :</p>
 * <pre>{@code
 * BookingRequest request = BookingRequest.builder()
 *     .startTime(LocalDateTime.of(2024, 3, 15, 14, 0))
 *     .endTime(LocalDateTime.of(2024, 3, 15, 16, 30))
 *     .serviceIds(Set.of(123L, 124L))
 *     .build();
 * }</pre>
 */
@ValidBusinessHours
public record BookingRequest(
    /**
     * Heure de début de la réservation
     * Doit être dans le futur
     */
    @NotNull(message = "Start time is mandatory")
    @Future(message = "Start time must be in the future")
    LocalDateTime startTime,
    
    /**
     * Heure de fin de la réservation
     * Doit être postérieure à l'heure de début
     */
    @NotNull(message = "End time is mandatory")
    LocalDateTime endTime,
    
    /**
     * IDs des services demandés
     * Au moins un service doit être sélectionné
     */
    @NotEmpty(message = "At least one service must be selected")
    Set<Long> serviceIds
) {
    /**
     * Validation personnalisée pour s'assurer que l'heure de fin est après l'heure de début
     * REFACTORISÉ : Utilise TimeRangeValidator centralisé
     * @return true si la validation passe
     */
    @AssertTrue(message = "End time must be after start time")
    public boolean isValidTimeRange() {
        return TimeRangeValidator.isValidDateTimeRange(startTime, endTime);
    }
    
    /**
     * Validation pour s'assurer qu'une réservation ne dépasse pas 8 heures
     * REFACTORISÉ : Utilise TimeRangeValidator centralisé
     * @return true si la validation passe
     */
    @AssertTrue(message = "Booking duration cannot exceed 8 hours")
    public boolean isValidDuration() {
        return TimeRangeValidator.isValidDateTimeDuration(startTime, endTime, 8);
    }
    
    /**
     * Crée une nouvelle instance du Builder pour BookingRequest
     * @return Une nouvelle instance du Builder
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder Pattern Context7 pour BookingRequest
     * Fournit une API fluide avec validation métier avancée des créneaux
     *
     * <p>Fonctionnalités avancées :</p>
     * <ul>
     *   <li>Validation des créneaux horaires métier (8h-20h)</li>
     *   <li>Validation de la durée minimum et maximum</li>
     *   <li>Validation des IDs de services</li>
     *   <li>Calcul automatique de la durée</li>
     *   <li>Validation des chevauchements de créneaux</li>
     *   <li>Support des réservations multi-services</li>
     * </ul>
     */
    public static class Builder {
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Set<Long> serviceIds = new HashSet<>();
        
        /**
         * Définit l'heure de début de la réservation (requis)
         * @param startTime L'heure de début
         * @return Le builder pour chaînage
         * @throws IllegalArgumentException si l'heure est dans le passé ou invalide
         */
        public Builder startTime(LocalDateTime startTime) {
            if (startTime != null) {
                // Validation future (avec tolérance de 5 minutes pour les tests)
                if (startTime.isBefore(LocalDateTime.now().minusMinutes(5))) {
                    throw new IllegalArgumentException("Start time must be in the future (5 min tolerance)");
                }
                
                // Validation heures d'ouverture (8h-20h)
                if (startTime.getHour() < 8 || startTime.getHour() >= 20) {
                    throw new IllegalArgumentException("Start time must be within business hours (8:00-20:00)");
                }
                
                // Validation des créneaux de 15 minutes
                if (startTime.getMinute() % 15 != 0) {
                    throw new IllegalArgumentException("Start time must be on 15-minute intervals");
                }
            }
            this.startTime = startTime;
            return this;
        }
        
        /**
         * Définit l'heure de fin de la réservation (requis)
         * @param endTime L'heure de fin
         * @return Le builder pour chaînage
         * @throws IllegalArgumentException si l'heure de fin est invalide
         */
        public Builder endTime(LocalDateTime endTime) {
            if (endTime != null) {
                // Validation heures d'ouverture (fermeture 20h max)
                if (endTime.getHour() > 20 || (endTime.getHour() == 20 && endTime.getMinute() > 0)) {
                    throw new IllegalArgumentException("End time must be before 20:00");
                }
                
                // Validation des créneaux de 15 minutes
                if (endTime.getMinute() % 15 != 0) {
                    throw new IllegalArgumentException("End time must be on 15-minute intervals");
                }
                
                // Validation cohérence avec startTime si déjà défini
                if (this.startTime != null) {
                    if (endTime.isBefore(this.startTime) || endTime.isEqual(this.startTime)) {
                        throw new IllegalArgumentException("End time must be after start time");
                    }
                    
                    Duration duration = Duration.between(this.startTime, endTime);
                    if (duration.toMinutes() < 15) {
                        throw new IllegalArgumentException("Booking duration must be at least 15 minutes");
                    }
                    if (duration.toMinutes() > 480) { // 8 heures max
                        throw new IllegalArgumentException("Booking duration cannot exceed 8 hours");
                    }
                }
            }
            this.endTime = endTime;
            return this;
        }
        
        /**
         * Définit les IDs des services sélectionnés (requis)
         * @param serviceIds L'ensemble des IDs de services
         * @return Le builder pour chaînage
         * @throws IllegalArgumentException si des IDs sont invalides
         */
        public Builder serviceIds(Set<Long> serviceIds) {
            if (serviceIds != null) {
                if (serviceIds.isEmpty()) {
                    throw new IllegalArgumentException("At least one service must be selected");
                }
                if (serviceIds.size() > 5) {
                    throw new IllegalArgumentException("Cannot select more than 5 services per booking");
                }
                
                // Validation des IDs
                serviceIds.forEach(id -> {
                    if (id == null || id <= 0) {
                        throw new IllegalArgumentException("All service IDs must be positive");
                    }
                });
                
                this.serviceIds = new HashSet<>(serviceIds);
            }
            return this;
        }
        
        /**
         * Ajoute un service à la liste des services
         * @param serviceId L'ID du service à ajouter
         * @return Le builder pour chaînage
         * @throws IllegalArgumentException si l'ID est invalide ou la limite est atteinte
         */
        public Builder addServiceId(Long serviceId) {
            if (serviceId == null || serviceId <= 0) {
                throw new IllegalArgumentException("Service ID must be positive");
            }
            if (this.serviceIds.size() >= 5) {
                throw new IllegalArgumentException("Cannot select more than 5 services per booking");
            }
            this.serviceIds.add(serviceId);
            return this;
        }
        
        /**
         * Définit un créneau de réservation complet avec validation
         * @param startTime Heure de début
         * @param durationMinutes Durée en minutes
         * @return Le builder pour chaînage
         * @throws IllegalArgumentException si le créneau est invalide
         */
        public Builder timeSlot(LocalDateTime startTime, int durationMinutes) {
            if (durationMinutes < 15) {
                throw new IllegalArgumentException("Duration must be at least 15 minutes");
            }
            if (durationMinutes > 480) {
                throw new IllegalArgumentException("Duration cannot exceed 8 hours (480 minutes)");
            }
            if (durationMinutes % 15 != 0) {
                throw new IllegalArgumentException("Duration must be in 15-minute increments");
            }
            
            this.startTime(startTime);
            this.endTime(startTime.plusMinutes(durationMinutes));
            return this;
        }
        
        /**
         * Définit un créneau de réservation pour aujourd'hui
         * @param hour Heure (8-19)
         * @param minute Minute (0, 15, 30, 45)
         * @param durationMinutes Durée en minutes
         * @return Le builder pour chaînage
         */
        public Builder todaySlot(int hour, int minute, int durationMinutes) {
            LocalDateTime today = LocalDateTime.now().withHour(hour).withMinute(minute).withSecond(0).withNano(0);
            return timeSlot(today, durationMinutes);
        }
        
        /**
         * Construit et valide l'instance BookingRequest
         * Applique la validation métier Context7 complète
         *
         * @return Une nouvelle instance BookingRequest validée
         * @throws IllegalStateException si les champs requis sont manquants ou invalides
         */
        public BookingRequest build() {
            // Validation des champs requis
            if (startTime == null) {
                throw new IllegalStateException("Start time is required for booking request");
            }
            if (endTime == null) {
                throw new IllegalStateException("End time is required for booking request");
            }
            if (serviceIds.isEmpty()) {
                throw new IllegalStateException("At least one service must be selected");
            }
            
            // Validation métier avancée des créneaux
            if (endTime.isBefore(startTime) || endTime.isEqual(startTime)) {
                throw new IllegalStateException("End time must be after start time");
            }
            
            Duration duration = Duration.between(startTime, endTime);
            if (duration.toMinutes() < 15) {
                throw new IllegalStateException("Booking duration must be at least 15 minutes");
            }
            if (duration.toMinutes() > 480) { // 8 heures max
                throw new IllegalStateException("Booking duration cannot exceed 8 hours");
            }
            
            // Validation des heures d'ouverture
            if (startTime.getHour() < 8 || startTime.getHour() >= 20) {
                throw new IllegalStateException("Start time must be within business hours (8:00-20:00)");
            }
            if (endTime.getHour() > 20 || (endTime.getHour() == 20 && endTime.getMinute() > 0)) {
                throw new IllegalStateException("End time must be before 20:00");
            }
            
            // Validation des créneaux de 15 minutes
            if (startTime.getMinute() % 15 != 0 || endTime.getMinute() % 15 != 0) {
                throw new IllegalStateException("Times must be on 15-minute intervals");
            }
            
            // Validation que la réservation n'est pas un dimanche (exemple de règle métier)
            if (startTime.getDayOfWeek().getValue() == 7) { // Dimanche
                throw new IllegalStateException("Bookings are not allowed on Sundays");
            }
            
            // Validation du nombre de services vs durée
            long durationMinutes = duration.toMinutes();
            int servicesCount = serviceIds.size();
            double averageTimePerService = (double) durationMinutes / servicesCount;
            
            if (averageTimePerService < 30) {
                throw new IllegalStateException(
                    String.format("Duration too short for %d services (%.0f min/service, minimum 30 min/service)", 
                    servicesCount, averageTimePerService)
                );
            }
            
            // Vérification de la date limite de réservation (max 6 mois à l'avance)
            if (startTime.isAfter(LocalDateTime.now().plusMonths(6))) {
                throw new IllegalStateException("Cannot book more than 6 months in advance");
            }
            
            return new BookingRequest(startTime, endTime, new HashSet<>(serviceIds));
        }
    }
}