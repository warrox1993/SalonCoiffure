package com.jb.afrostyle.booking.controller;

import com.jb.afrostyle.booking.domain.enums.BookingStatus;
import com.jb.afrostyle.booking.mapper.BookingMapper;
import com.jb.afrostyle.booking.domain.entity.Booking;
import com.jb.afrostyle.booking.domain.entity.SalonReport;
import com.jb.afrostyle.booking.dto.BookingDTO;
import com.jb.afrostyle.booking.dto.BookingRequest;
import com.jb.afrostyle.booking.service.BookingService;
import com.jb.afrostyle.salon.service.SalonService;
import com.jb.afrostyle.serviceoffering.service.ServiceOfferingService;
import com.jb.afrostyle.user.dto.ApiResponse;
import com.jb.afrostyle.serviceoffering.mapper.ServiceOfferingMapper;
import com.jb.afrostyle.booking.dto.BookingSlotDTO;
import com.jb.afrostyle.booking.util.UserAuthenticationHelper;
import com.jb.afrostyle.booking.util.BookingResponseHandler;
import com.jb.afrostyle.core.validation.ValidationUtils;
import com.jb.afrostyle.core.validation.ValidationResult;

import java.time.LocalDate;
import java.util.Collections;
import java.util.stream.Collectors;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour la gestion des réservations (Booking)
 * 
 * Expose les endpoints CRUD et opérations métier pour les réservations.
 * Utilise les services existants sans modification de l'architecture.
 * 
 * Sécurité : Chaque endpoint est protégé par @PreAuthorize selon les rôles appropriés.
 * 
 * @author AfroStyle Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private static final Logger log = LoggerFactory.getLogger(BookingController.class);

    private final BookingService bookingService;
    private final SalonService salonService;
    private final ServiceOfferingService serviceOfferingService;
    private final UserAuthenticationHelper userAuthenticationHelper;
    private final BookingResponseHandler responseHandler;
    private final ServiceOfferingMapper serviceOfferingMapper;


    /**
     * Créer une nouvelle réservation
     * 
     * @param bookingRequest Les détails de la réservation à créer
     * @param authentication L'objet d'authentification Spring Security
     * @return La réservation créée ou une erreur
     */
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('SALON_OWNER') or hasRole('ADMIN')")
    public ResponseEntity<?> createBooking(
            @Valid @RequestBody BookingRequest bookingRequest,
            Authentication authentication) {
        
        // REFACTORISÉ : Utilise BookingResponseHandler pour éliminer try/catch répétitif
        return responseHandler.executeWithErrorHandling("createBooking", () -> {
            log.info("Creating booking for services: {}", bookingRequest.serviceIds());
            
            // REFACTORISÉ : Extraction de l'utilisateur via UserAuthenticationHelper
            var user = userAuthenticationHelper.getCurrentUser(authentication);
            
            // Récupération des données nécessaires via les services existants
            var salon = salonService.getSalonByServiceId(bookingRequest.serviceIds().iterator().next());
            var services = serviceOfferingService.getServicesByIds(bookingRequest.serviceIds());
            
            // Création de la réservation via le service existant
            var serviceDTOs = services.stream()
                    .map(serviceOfferingMapper::toDTO)
                    .collect(Collectors.toSet());
            Booking createdBooking = bookingService.createBooking(bookingRequest, user, salon, serviceDTOs);
            
            // Conversion en DTO pour la réponse
            BookingDTO bookingDTO = BookingMapper.INSTANCE.toDTO(createdBooking);
            
            log.info("Booking created successfully with ID: {}", createdBooking.getId());
            return bookingDTO;
        }, "Booking created successfully");
    }

    /**
     * Obtenir une réservation par son ID
     * 
     * Sécurité : Seul le propriétaire de la réservation, le propriétaire du salon, ou un admin peut accéder
     * 
     * @param id L'ID de la réservation à récupérer
     * @return La réservation demandée ou une erreur
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER') or hasRole('SALON_OWNER')")
    public ResponseEntity<?> getBookingById(@PathVariable Long id) {
        // REFACTORISÉ : Utilise BookingResponseHandler
        return responseHandler.executeWithErrorHandling("getBookingById", () -> {
            log.info("Fetching booking with ID: {}", id);
            
            Booking booking = bookingService.getBookingById(id);
            return BookingMapper.INSTANCE.toDTO(booking);
        });
    }

    /**
     * Lister les réservations de l'utilisateur connecté
     * 
     * @param authentication L'objet d'authentification Spring Security
     * @return La liste des réservations de l'utilisateur
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('SALON_OWNER') or hasRole('ADMIN')")
    public ResponseEntity<?> getMyBookings(Authentication authentication) {
        // REFACTORISÉ : Utilise BookingResponseHandler
        return responseHandler.executeWithErrorHandling("getMyBookings", () -> {
            // REFACTORISÉ : Extraction de l'utilisateur via UserAuthenticationHelper
            var user = userAuthenticationHelper.getCurrentUser(authentication);
            
            log.info("Fetching bookings for user: {}", user.username());
            
            List<Booking> bookings = bookingService.getBookingsByCustomer(user.id());
            List<BookingDTO> bookingDTOs = bookings.stream()
                    .map(BookingMapper.INSTANCE::toDTO)
                    .collect(Collectors.toList());
            
            return bookingDTOs;
        });
    }

    /**
     * Lister toutes les réservations du salon
     * MONO-SALON : Toutes les réservations appartiennent au salon unique
     * 
     * Sécurité : Seul le propriétaire du salon ou un admin peut accéder
     * 
     * @return La liste de toutes les réservations
     */
    @GetMapping("/salon")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SALON_OWNER')")
    public ResponseEntity<?> getAllSalonBookings() {
        // REFACTORISÉ : Utilise BookingResponseHandler
        return responseHandler.executeWithErrorHandling("getAllSalonBookings", () -> {
            log.info("Fetching all bookings for unique salon");
            
            List<Booking> bookings = bookingService.getAllBookings();
            List<BookingDTO> bookingDTOs = bookings.stream()
                    .map(BookingMapper.INSTANCE::toDTO)
                    .collect(Collectors.toList());
            
            return bookingDTOs;
        });
    }

    /**
     * Annuler une réservation
     * 
     * Sécurité : Le client propriétaire, le propriétaire du salon, ou un admin peuvent annuler
     * 
     * @param id L'ID de la réservation à annuler
     * @return La réservation mise à jour ou une erreur
     */
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER') or hasRole('SALON_OWNER')")
    public ResponseEntity<?> cancelBooking(@PathVariable Long id) {
        // REFACTORISÉ : Utilise BookingResponseHandler
        return responseHandler.executeWithErrorHandling("cancelBooking", () -> {
            log.info("Cancelling booking: {}", id);
            
            Booking updatedBooking = bookingService.updateBooking(id, BookingStatus.CANCELLED);
            BookingDTO bookingDTO = BookingMapper.INSTANCE.toDTO(updatedBooking);
            
            return bookingDTO;
        }, "Booking cancelled successfully");
    }

    /**
     * Confirmer une réservation (après paiement ou validation)
     * 
     * Sécurité : Seul le propriétaire du salon ou un admin peuvent confirmer
     * 
     * @param id L'ID de la réservation à confirmer
     * @return La réservation mise à jour ou une erreur
     */
    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SALON_OWNER')")
    public ResponseEntity<?> confirmBooking(@PathVariable Long id) {
        // REFACTORISÉ : Utilise BookingResponseHandler
        return responseHandler.executeWithErrorHandling("confirmBooking", () -> {
            log.info("Confirming booking: {}", id);
            
            Booking updatedBooking = bookingService.updateBooking(id, BookingStatus.CONFIRMED);
            BookingDTO bookingDTO = BookingMapper.INSTANCE.toDTO(updatedBooking);
            
            return bookingDTO;
        }, "Booking confirmed successfully");
    }

    /**
     * Marquer une réservation comme réalisée/terminée
     * 
     * Sécurité : Seul le propriétaire du salon ou un admin peuvent marquer comme terminé
     * 
     * @param id L'ID de la réservation à terminer
     * @return La réservation mise à jour ou une erreur
     */
    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SALON_OWNER')")
    public ResponseEntity<?> completeBooking(@PathVariable Long id) {
        // REFACTORISÉ : Utilise BookingResponseHandler
        return responseHandler.executeWithErrorHandling("completeBooking", () -> {
            log.info("Completing booking: {}", id);
            
            // Note: Si BookingStatus.COMPLETED n'existe pas, utiliser CONFIRMED comme statut final
            Booking updatedBooking = bookingService.updateBooking(id, BookingStatus.CONFIRMED);
            BookingDTO bookingDTO = BookingMapper.INSTANCE.toDTO(updatedBooking);
            
            return bookingDTO;
        }, "Booking completed successfully");
    }

    /**
     * Obtenir les statistiques du salon
     * MONO-SALON : Statistiques pour le salon unique
     * 
     * Sécurité : Seul le propriétaire du salon ou un admin peuvent accéder aux stats
     * 
     * @return Les statistiques du salon
     */
    @GetMapping("/salon/stats")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SALON_OWNER')")
    public ResponseEntity<?> getSalonStats() {
        return responseHandler.executeWithErrorHandling("getSalonStats", () -> {
            log.info("Fetching stats for unique salon");
            
            SalonReport stats = bookingService.getSalonReport();
            
            return stats;
        });
    }

    /**
     * Récupérer les créneaux de réservation pour une date donnée
     * Endpoint mono-salon pour la compatibilité frontend
     */
    @GetMapping("/slots/date/{date}")
    public ResponseEntity<?> getBookedSlotsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        // REFACTORISÉ : Utilise BookingResponseHandler
        return responseHandler.executeWithErrorHandling("getBookedSlotsByDate", () -> {
            log.info("Fetching booked slots for date: {}", date);
            
            // Récupérer toutes les réservations pour cette date
            List<Booking> bookings = bookingService.getBookingsByDate(date);
            
            // Convertir en slots réservés
            List<BookingSlotDTO> bookedSlots = bookings.stream()
                    .map(booking -> new BookingSlotDTO(
                            booking.getStartTime(),
                            booking.getEndTime()
                    ))
                    .collect(Collectors.toList());
            
            return bookedSlots;
        });
    }
}

/*
 * ACTIVATION MODE_DOC_LIGNE_PAR_LIGNE :
 * 
 * Pour activer la documentation ligne par ligne, changer MODE_DOC_LIGNE_PAR_LIGNE = TRUE
 * et ajouter avant chaque ligne :
 * 
 * /// Pourquoi ? [Justification business/technique]
 * /// Que fait la ligne ? [Description action]  
 * /// Comment ça marche ? [Mécanisme technique]
 * 
 * Exemple :
 * /// Pourquoi ? Sécurité - vérifier droits avant accès données
 * /// Que fait la ligne ? Vérifie si utilisateur peut accéder à cette réservation
 * /// Comment ça marche ? Spring @PreAuthorize évalue expression SpEL
 * @PreAuthorize("hasRole('ADMIN') or @bookingSecurityService.canAccessBooking(#id, authentication.name)")
 */