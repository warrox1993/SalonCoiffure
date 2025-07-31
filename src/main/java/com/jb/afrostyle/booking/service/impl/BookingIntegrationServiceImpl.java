package com.jb.afrostyle.booking.service.impl;

import com.jb.afrostyle.booking.domain.entity.Booking;
import com.jb.afrostyle.booking.repository.BookingRepository;
import com.jb.afrostyle.booking.service.BookingIntegrationService;
import com.jb.afrostyle.integrations.google.googleCalendar.service.GoogleCalendarService;
import com.jb.afrostyle.salon.payload.dto.SalonDTO;
import com.jb.afrostyle.service.email.EmailService;
import com.jb.afrostyle.serviceoffering.payload.dto.ServiceDTO;
import com.jb.afrostyle.user.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implémentation du service d'intégration des réservations
 * REFACTORISÉ : Extrait de BookingServiceImpl.createBooking()
 */
@Service
@RequiredArgsConstructor
public class BookingIntegrationServiceImpl implements BookingIntegrationService {
    
    private static final Logger log = LoggerFactory.getLogger(BookingIntegrationServiceImpl.class);
    
    private final GoogleCalendarService googleCalendarService;
    private final EmailService emailService;
    private final BookingRepository bookingRepository;
    
    @Override
    public Booking handlePostBookingIntegrations(
            Booking booking,
            UserDTO user,
            SalonDTO salon,
            Set<ServiceDTO> services) {
        
        log.info("🔗 Starting integrations for booking ID: {}", booking.getId());
        
        // ÉTAPE 1 : Enrichir les données pour les intégrations
        booking = enrichBookingForIntegrations(booking, user, salon);
        
        // ÉTAPE 2 : Créer l'événement Google Calendar
        String calendarEventId = createGoogleCalendarEvent(booking);
        if (calendarEventId != null) {
            booking.setGoogleCalendarEventId(calendarEventId);
            booking = bookingRepository.save(booking);
        }
        
        // ÉTAPE 3 : Envoyer l'email de confirmation
        sendBookingConfirmationEmail(booking, user, salon, services);
        
        log.info("✅ All integrations completed for booking ID: {}", booking.getId());
        return booking;
    }
    
    @Override
    public String createGoogleCalendarEvent(Booking booking) {
        try {
            log.info("📅 Creating Google Calendar event for booking: {}", booking.getId());
            
            String calendarEventId = googleCalendarService.createCalendarEvent(booking);
            
            if (calendarEventId != null) {
                log.info("✅ Google Calendar event created with ID: {}", calendarEventId);
                return calendarEventId;
            } else {
                log.warn("⚠️ Google Calendar event could not be created");
                return null;
            }
            
        } catch (Exception e) {
            log.error("❌ Error creating Google Calendar event for booking {}: {}", 
                    booking.getId(), e.getMessage());
            // Ne pas faire échouer la réservation si le calendrier échoue
            return null;
        }
    }
    
    @Override
    public void sendBookingConfirmationEmail(
            Booking booking,
            UserDTO user,
            SalonDTO salon,
            Set<ServiceDTO> services) {
        
        try {
            log.info("📧 Sending booking confirmation email for booking: {}", booking.getId());
            
            // Formater les détails des services pour l'email
            String serviceDetails = formatServiceDetailsForEmail(booking, services);
            
            // Envoyer l'email via EmailService
            emailService.sendBookingConfirmationEmail(
                    user.email(),
                    user.fullName(),
                    "AfroStyle Salon", // salonName - TODO: get from salon entity
                    formatBookingDetailsForEmail(booking)
            );
            
            log.info("✅ Booking confirmation email sent to: {}", user.email());
            
        } catch (Exception e) {
            log.error("❌ Error sending booking confirmation email for booking {}: {}", 
                    booking.getId(), e.getMessage());
            // Ne pas faire échouer la réservation si l'email échoue
        }
    }
    
    /**
     * Enrichit la réservation avec les données nécessaires aux intégrations
     * REFACTORISÉ : Extrait de BookingServiceImpl
     */
    private Booking enrichBookingForIntegrations(Booking booking, UserDTO user, SalonDTO salon) {
        // Enrichir les données pour Google Calendar
        booking.setUserName(user.fullName());
        booking.setSalonName("AfroStyle Salon"); // Nom fixe pour le salon unique
        booking.setBookingDate(booking.getStartTime());
        
        log.info("📝 Booking enriched with integration data");
        return booking;
    }
    
    /**
     * Formate les détails de la réservation pour l'email
     * REFACTORISÉ : Extrait de BookingServiceImpl
     */
    private String formatBookingDetailsForEmail(Booking booking) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        StringBuilder details = new StringBuilder();
        details.append("Réservation #").append(booking.getId()).append("\\n");
        details.append("Date: ").append(booking.getStartTime().format(dateFormatter)).append("\\n");
        details.append("Horaire: ").append(booking.getStartTime().format(timeFormatter))
               .append(" - ").append(booking.getEndTime().format(timeFormatter)).append("\\n");
        details.append("Prix total: ").append(booking.getTotalPrice()).append("€");
        
        return details.toString();
    }
    
    /**
     * Formate les détails des services pour l'email
     * REFACTORISÉ : Extrait de BookingServiceImpl
     */
    private String formatServiceDetailsForEmail(Booking booking, Set<ServiceDTO> services) {
        if (services == null || services.isEmpty()) {
            return "Aucun service spécifié";
        }
        
        return services.stream()
                .map(service -> "• " + service.name() + " (" + service.duration() + "min, " + service.price() + "€)")
                .collect(Collectors.joining("\\n"));
    }
}