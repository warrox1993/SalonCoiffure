package com.jb.afrostyle.booking.repository;

import com.jb.afrostyle.booking.domain.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByCustomerId(Long customerId);

    @Query("SELECT b FROM Booking b WHERE DATE(b.startTime) = :date")
    List<Booking> findByStartTimeDate(@Param("date") LocalDate date);
    
    /**
     * Vérifie s'il existe des réservations en conflit avec la plage horaire donnée
     * Exclut les réservations annulées
     */
    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE " +
           "(b.startTime < :endTime AND b.endTime > :startTime) " +
           "AND b.status != 'CANCELLED'")
    boolean existsConflictingBookings(@Param("startTime") LocalDateTime startTime, 
                                     @Param("endTime") LocalDateTime endTime);
    
    /**
     * Trouve toutes les réservations en conflit avec la plage horaire donnée
     * Exclut les réservations annulées
     */
    @Query("SELECT b FROM Booking b WHERE " +
           "(b.startTime < :endTime AND b.endTime > :startTime) " +
           "AND b.status != 'CANCELLED'")
    List<Booking> findConflictingBookings(@Param("startTime") LocalDateTime startTime, 
                                         @Param("endTime") LocalDateTime endTime);
    
    /**
     * Trouve les réservations dans une plage de dates
     * Utilisé pour les statistiques et la récupération de créneaux
     */
    List<Booking> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
}