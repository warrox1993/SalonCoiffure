package com.jb.afrostyle.booking.service.impl;

import com.jb.afrostyle.booking.service.BookingCalculation;
import com.jb.afrostyle.booking.service.BookingCalculationService;
import com.jb.afrostyle.serviceoffering.payload.dto.ServiceDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Implémentation du service de calcul des réservations
 * REFACTORISÉ : Extrait de BookingServiceImpl.createBooking()
 */
@Service
public class BookingCalculationServiceImpl implements BookingCalculationService {
    
    private static final Logger log = LoggerFactory.getLogger(BookingCalculationServiceImpl.class);
    
    @Override
    public int calculateTotalDuration(Set<ServiceDTO> services) {
        if (services == null || services.isEmpty()) {
            log.warn("⚠️ No services provided for duration calculation");
            return 0;
        }
        
        int totalMinutes = services.stream()
                .mapToInt(ServiceDTO::duration)
                .sum();
        
        log.info("📊 Calculated total duration: {} minutes for {} services", 
                totalMinutes, services.size());
        
        return totalMinutes;
    }
    
    @Override  
    public BigDecimal calculateTotalPrice(Set<ServiceDTO> services) {
        if (services == null || services.isEmpty()) {
            log.warn("⚠️ No services provided for price calculation");
            return BigDecimal.ZERO;
        }
        
        BigDecimal totalPrice = services.stream()
                .map(ServiceDTO::price)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        log.info("💰 Calculated total price: {}€ for {} services", 
                totalPrice, services.size());
        
        return totalPrice;
    }
    
    @Override
    public BookingCalculation calculateBookingMetrics(Set<ServiceDTO> services) {
        if (services == null || services.isEmpty()) {
            log.warn("⚠️ No services provided for metrics calculation");
            return BookingCalculation.create(0, BigDecimal.ZERO, 0);
        }
        
        int duration = calculateTotalDuration(services);
        BigDecimal price = calculateTotalPrice(services);
        int servicesCount = services.size();
        
        BookingCalculation calculation = BookingCalculation.create(duration, price, servicesCount);
        
        log.info("📈 Booking metrics calculated: {}min, {}€, {} services", 
                duration, price, servicesCount);
        
        return calculation;
    }
}