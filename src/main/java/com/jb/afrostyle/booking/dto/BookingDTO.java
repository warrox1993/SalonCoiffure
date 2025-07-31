package com.jb.afrostyle.booking.dto;

import com.jb.afrostyle.booking.domain.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Booking DTO avec Builder Pattern Context7 pour une construction fluide et validation métier
 * Intègre calculs automatiques de durée et validation des créneaux horaires
 *
 * <p>Exemple d'utilisation :</p>
 * <pre>{@code
 * BookingDTO booking = BookingDTO.builder()
 *     .customerId(456L)
 *     .serviceIds(Set.of(123L, 124L))
 *     .startTime(LocalDateTime.of(2024, 3, 15, 10, 0))
 *     .endTime(LocalDateTime.of(2024, 3, 15, 12, 30))
 *     .totalPrice(new BigDecimal("75.00"))
 *     .userName("Marie Dupont")
 *     .salonName("AfroStyle Salon")
 *     .build();
 * }</pre>
 */
public record BookingDTO(
        Long id,
        Long serviceId,
        Long customerId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Set<Long> serviceIds,
        BookingStatus status,
        BigDecimal totalPrice,
        Integer totalServices,
        // Ajouté pour éviter recalcul côté frontend (mentionné dans CLAUDE.md)
        Integer totalDuration, // en minutes
        String userName,
        String serviceName,
        String salonName,
        LocalDateTime bookingDate,
        String notes,
        String googleCalendarEventId
) {
    public BookingDTO {
        // Valeurs par défaut
        if (status == null) status = BookingStatus.PENDING;
        if (totalServices == null) totalServices = 1;
    }
    
    /**
     * Crée une nouvelle instance du Builder pour BookingDTO
     * @return Une nouvelle instance du Builder
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder Pattern Context7 pour BookingDTO
     * Fournit une API fluide avec validation métier intégrée et calculs automatiques
     *
     * <p>Fonctionnalités avancées :</p>
     * <ul>
     *   <li>Calcul automatique de la durée totale en minutes</li>
     *   <li>Validation des créneaux horaires métier</li>
     *   <li>Calcul automatique du nombre de services</li>
     *   <li>Validation des prix et cohérence des données</li>
     *   <li>Génération automatique des timestamps</li>
     *   <li>Validation des IDs de services et cohérence</li>
     * </ul>
     */
    public static class Builder {
        private Long id;
        private Long serviceId;
        private Long customerId;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Set<Long> serviceIds = new HashSet<>();
        private BookingStatus status = BookingStatus.PENDING;
        private BigDecimal totalPrice;
        private Integer totalServices;
        private Integer totalDuration;
        private String userName;
        private String serviceName;
        private String salonName;
        private LocalDateTime bookingDate;
        private String notes;
        private String googleCalendarEventId;
        
        /**
         * Définit l'ID de la réservation
         * @param id L'identifiant unique de la réservation
         * @return Le builder pour chaînage
         */
        public Builder id(Long id) {
            this.id = id;
            return this;
        }
        
        /**
         * Définit l'ID du service principal (pour compatibilité legacy)
         * @param serviceId L'identifiant du service principal
         * @return Le builder pour chaînage
         * @throws IllegalArgumentException si serviceId est négatif
         */
        public Builder serviceId(Long serviceId) {
            if (serviceId != null && serviceId <= 0) {
                throw new IllegalArgumentException("Service ID must be positive");
            }
            this.serviceId = serviceId;
            return this;
        }
        
        /**
         * Définit l'ID du client (requis)
         * @param customerId L'identifiant du client
         * @return Le builder pour chaînage
         * @throws IllegalArgumentException si customerId est null ou négatif
         */
        public Builder customerId(Long customerId) {
            if (customerId != null && customerId <= 0) {
                throw new IllegalArgumentException("Customer ID must be positive");
            }
            this.customerId = customerId;
            return this;
        }
        
        /**
         * Définit l'heure de début de la réservation (requis)
         * @param startTime L'heure de début
         * @return Le builder pour chaînage
         * @throws IllegalArgumentException si l'heure est dans le passé
         */
        public Builder startTime(LocalDateTime startTime) {
            if (startTime != null && startTime.isBefore(LocalDateTime.now().minusMinutes(15))) {
                throw new IllegalArgumentException("Start time cannot be more than 15 minutes in the past");
            }
            this.startTime = startTime;
            return this;
        }
        
        /**
         * Définit l'heure de fin de la réservation (requis)
         * @param endTime L'heure de fin
         * @return Le builder pour chaînage
         */
        public Builder endTime(LocalDateTime endTime) {
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
         * @throws IllegalArgumentException si l'ID est invalide
         */
        public Builder addServiceId(Long serviceId) {
            if (serviceId == null || serviceId <= 0) {
                throw new IllegalArgumentException("Service ID must be positive");
            }
            this.serviceIds.add(serviceId);
            return this;
        }
        
        /**
         * Définit le statut de la réservation (par défaut PENDING)
         * @param status Le statut de la réservation
         * @return Le builder pour chaînage
         */
        public Builder status(BookingStatus status) {
            this.status = status != null ? status : BookingStatus.PENDING;
            return this;
        }
        
        /**
         * Définit le prix total de la réservation
         * @param totalPrice Le prix total
         * @return Le builder pour chaînage
         * @throws IllegalArgumentException si le prix est négatif ou dépasse 1000€
         */
        public Builder totalPrice(BigDecimal totalPrice) {
            if (totalPrice != null) {
                if (totalPrice.compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException("Total price cannot be negative");
                }
                if (totalPrice.compareTo(new BigDecimal("1000.00")) > 0) {
                    throw new IllegalArgumentException("Total price cannot exceed 1000.00 EUR");
                }
            }
            this.totalPrice = totalPrice;
            return this;
        }
        
        /**
         * Définit le nombre de services (calculé automatiquement si non fourni)
         * @param totalServices Le nombre de services
         * @return Le builder pour chaînage
         * @throws IllegalArgumentException si le nombre est négatif
         */
        public Builder totalServices(Integer totalServices) {
            if (totalServices != null && totalServices < 0) {
                throw new IllegalArgumentException("Total services cannot be negative");
            }
            this.totalServices = totalServices;
            return this;
        }
        
        /**
         * Définit la durée totale en minutes (calculée automatiquement si non fournie)
         * @param totalDuration La durée en minutes
         * @return Le builder pour chaînage
         * @throws IllegalArgumentException si la durée est négative ou trop longue
         */
        public Builder totalDuration(Integer totalDuration) {
            if (totalDuration != null) {
                if (totalDuration < 0) {
                    throw new IllegalArgumentException("Total duration cannot be negative");
                }
                if (totalDuration > 480) { // 8 heures max
                    throw new IllegalArgumentException("Total duration cannot exceed 8 hours (480 minutes)");
                }
            }
            this.totalDuration = totalDuration;
            return this;
        }
        
        /**
         * Définit le nom de l'utilisateur
         * @param userName Le nom de l'utilisateur
         * @return Le builder pour chaînage
         * @throws IllegalArgumentException si le nom dépasse 100 caractères
         */
        public Builder userName(String userName) {
            if (userName != null && userName.length() > 100) {
                throw new IllegalArgumentException("User name cannot exceed 100 characters");
            }
            this.userName = userName;
            return this;
        }
        
        /**
         * Définit le nom du service principal
         * @param serviceName Le nom du service
         * @return Le builder pour chaînage
         * @throws IllegalArgumentException si le nom dépasse 200 caractères
         */
        public Builder serviceName(String serviceName) {
            if (serviceName != null && serviceName.length() > 200) {
                throw new IllegalArgumentException("Service name cannot exceed 200 characters");
            }
            this.serviceName = serviceName;
            return this;
        }
        
        /**
         * Définit le nom du salon
         * @param salonName Le nom du salon
         * @return Le builder pour chaînage
         * @throws IllegalArgumentException si le nom dépasse 100 caractères
         */
        public Builder salonName(String salonName) {
            if (salonName != null && salonName.length() > 100) {
                throw new IllegalArgumentException("Salon name cannot exceed 100 characters");
            }
            this.salonName = salonName;
            return this;
        }
        
        /**
         * Définit la date de réservation (automatique si non fournie)
         * @param bookingDate La date de réservation
         * @return Le builder pour chaînage
         */
        public Builder bookingDate(LocalDateTime bookingDate) {
            this.bookingDate = bookingDate;
            return this;
        }
        
        /**
         * Définit les notes de la réservation
         * @param notes Les notes
         * @return Le builder pour chaînage
         * @throws IllegalArgumentException si les notes dépassent 500 caractères
         */
        public Builder notes(String notes) {
            if (notes != null && notes.length() > 500) {
                throw new IllegalArgumentException("Notes cannot exceed 500 characters");
            }
            this.notes = notes;
            return this;
        }
        
        /**
         * Définit l'ID de l'événement Google Calendar
         * @param googleCalendarEventId L'ID de l'événement
         * @return Le builder pour chaînage
         */
        public Builder googleCalendarEventId(String googleCalendarEventId) {
            this.googleCalendarEventId = googleCalendarEventId;
            return this;
        }
        
        /**
         * Construit et valide l'instance BookingDTO
         * Applique la validation métier Context7 et calcule les valeurs automatiques
         *
         * @return Une nouvelle instance BookingDTO validée
         * @throws IllegalStateException si les champs requis sont manquants ou invalides
         */
        public BookingDTO build() {
            // Validation des champs requis
            if (customerId == null) {
                throw new IllegalStateException("Customer ID is required for booking creation");
            }
            if (startTime == null) {
                throw new IllegalStateException("Start time is required for booking creation");
            }
            if (endTime == null) {
                throw new IllegalStateException("End time is required for booking creation");
            }
            if (serviceIds.isEmpty()) {
                throw new IllegalStateException("At least one service must be selected");
            }
            
            // Validation métier des créneaux horaires
            if (endTime.isBefore(startTime) || endTime.isEqual(startTime)) {
                throw new IllegalStateException("End time must be after start time");
            }
            
            Duration duration = Duration.between(startTime, endTime);
            if (duration.toMinutes() > 480) { // 8 heures max
                throw new IllegalStateException("Booking duration cannot exceed 8 hours");
            }
            if (duration.toMinutes() < 15) { // 15 minutes min
                throw new IllegalStateException("Booking duration must be at least 15 minutes");
            }
            
            // Validation des heures d'ouverture (8h-20h)
            if (startTime.getHour() < 8 || startTime.getHour() >= 20) {
                throw new IllegalStateException("Booking must be within business hours (8:00-20:00)");
            }
            if (endTime.getHour() > 20 || (endTime.getHour() == 20 && endTime.getMinute() > 0)) {
                throw new IllegalStateException("Booking must end before 20:00");
            }
            
            // Calculs automatiques
            if (totalDuration == null) {
                totalDuration = (int) duration.toMinutes();
            }
            if (totalServices == null) {
                totalServices = serviceIds.size();
            }
            if (bookingDate == null) {
                bookingDate = LocalDateTime.now();
            }
            
            // Cohérence serviceId legacy avec serviceIds
            if (serviceId == null && !serviceIds.isEmpty()) {
                serviceId = serviceIds.iterator().next(); // Premier service comme service principal
            }
            
            // Validation prix vs services
            if (totalPrice != null && totalServices != null && totalServices > 0) {
                BigDecimal pricePerService = totalPrice.divide(BigDecimal.valueOf(totalServices), 2, BigDecimal.ROUND_HALF_UP);
                if (pricePerService.compareTo(new BigDecimal("5.00")) < 0) {
                    throw new IllegalStateException("Price per service seems too low (< 5.00 EUR)");
                }
            }
            
            // Validation temporelle
            if (bookingDate != null && startTime != null && bookingDate.isAfter(startTime)) {
                throw new IllegalStateException("Booking date cannot be after start time");
            }
            
            return new BookingDTO(
                id, serviceId, customerId, startTime, endTime, 
                new HashSet<>(serviceIds), status, totalPrice, totalServices, 
                totalDuration, userName, serviceName, salonName, 
                bookingDate, notes, googleCalendarEventId
            );
        }
    }
}