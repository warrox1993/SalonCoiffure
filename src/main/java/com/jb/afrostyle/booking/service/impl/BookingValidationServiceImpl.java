package com.jb.afrostyle.booking.service.impl;

import com.jb.afrostyle.booking.dto.BookingRequest;
import com.jb.afrostyle.booking.repository.BookingRepository;
import com.jb.afrostyle.booking.service.*;
import com.jb.afrostyle.salon.payload.dto.SalonDTO;
import com.jb.afrostyle.serviceoffering.payload.dto.ServiceDTO;
import com.jb.afrostyle.serviceoffering.service.ServiceOfferingService;
import com.jb.afrostyle.serviceoffering.modal.ServiceOffering;
import com.jb.afrostyle.serviceoffering.mapper.ServiceOfferingMapper;
import com.jb.afrostyle.user.dto.UserDTO;
import com.jb.afrostyle.user.mapper.UserMapper;
import com.jb.afrostyle.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implémentation du service de validation des réservations
 * REFACTORISÉ : Extrait de BookingServiceImpl.createBooking()
 */
@Service
@RequiredArgsConstructor
public class BookingValidationServiceImpl implements BookingValidationService {
    
    private static final Logger log = LoggerFactory.getLogger(BookingValidationServiceImpl.class);
    
    private final UserService userService;
    private final ServiceOfferingService serviceOfferingService;
    private final BookingRepository bookingRepository;
    private final BookingCalculationService calculationService;
    private final UserMapper userMapper;
    
    @Override
    public BookingValidationResult validateBookingData(
            BookingRequest request,
            UserDTO user,
            SalonDTO salon,
            Set<ServiceDTO> services) throws Exception {
        
        log.info("🔍 Validating booking data for user {} with {} services", 
                user.id(), services.size());
        
        // ÉTAPE 1 : Valider l'utilisateur
        UserDTO validatedUser = validateUser(user.id());
        
        // ÉTAPE 2 : Valider les services
        Set<ServiceDTO> validatedServices = validateServices(request.serviceIds());
        
        // ÉTAPE 3 : Le salon est déjà validé (salon unique)
        SalonDTO validatedSalon = salon;
        
        // ÉTAPE 4 : Calculer les métriques
        BookingCalculation calculation = calculationService.calculateBookingMetrics(validatedServices);
        
        // ÉTAPE 5 : Calculer les heures avec la durée réelle
        LocalDateTime startTime = request.startTime();
        LocalDateTime endTime = startTime.plusMinutes(calculation.totalDuration());
        
        // ÉTAPE 6 : Vérifier la disponibilité du créneau
        validateTimeSlotAvailability(validatedSalon, startTime, endTime);
        
        // ÉTAPE 7 : Créer le résultat de validation
        BookingValidationResult result = BookingValidationResult.create(
                validatedUser,
                validatedSalon,
                validatedServices,
                startTime,
                endTime,
                calculation.totalDuration(),
                calculation.totalPrice()
        );
        
        log.info("✅ Booking validation successful - Duration: {}min, Price: {}€", 
                calculation.totalDuration(), calculation.totalPrice());
        
        return result;
    }
    
    @Override
    public void validateTimeSlotAvailability(
            SalonDTO salon,
            LocalDateTime startTime,
            LocalDateTime endTime) throws Exception {
        
        log.info("🕒 Checking time slot availability: {} to {}", startTime, endTime);
        
        // Vérifier les conflits avec d'autres réservations
        boolean hasConflict = bookingRepository.existsConflictingBookings(startTime, endTime);
        
        if (hasConflict) {
            log.warn("❌ Time slot conflict detected for {} to {}", startTime, endTime);
            throw new Exception("Selected time slot is not available. Please choose another time.");
        }
        
        log.info("✅ Time slot is available");
    }
    
    /**
     * Valide l'existence de l'utilisateur
     * REFACTORISÉ : Extrait de BookingServiceImpl
     */
    private UserDTO validateUser(Long userId) throws Exception {
        try {
            var user = userService.getUserById(userId);
            return userMapper.toDTO(user);
        } catch (Exception e) {
            log.error("❌ User validation failed for ID: {}", userId);
            throw new Exception("Invalid user: " + e.getMessage());
        }
    }
    
    /**
     * Valide les services demandés
     * REFACTORISÉ : Extrait de BookingServiceImpl
     */
    private Set<ServiceDTO> validateServices(Set<Long> serviceIds) throws Exception {
        try {
            Set<ServiceOffering> serviceEntities = serviceOfferingService.getServicesByIds(serviceIds);
            Set<ServiceDTO> services = serviceEntities.stream()
                    .map(ServiceOfferingMapper.INSTANCE::toDTO)
                    .collect(Collectors.toSet());
            
            if (services.isEmpty()) {
                throw new Exception("No valid services found");
            }
            
            log.info("✅ Validated {} services", services.size());
            return services;
            
        } catch (Exception e) {
            log.error("❌ Services validation failed for IDs: {}", serviceIds);
            throw new Exception("Invalid services: " + e.getMessage());
        }
    }
}