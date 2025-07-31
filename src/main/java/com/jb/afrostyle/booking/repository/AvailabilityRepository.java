package com.jb.afrostyle.booking.repository;

import com.jb.afrostyle.booking.domain.entity.SalonAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface AvailabilityRepository extends JpaRepository<SalonAvailability, Long> {

    // Trouver toutes les disponibilités pour une date donnée
    List<SalonAvailability> findByDate(LocalDate date);

    // Trouver toutes les disponibilités entre deux dates
    List<SalonAvailability> findByDateBetween(LocalDate startDate, LocalDate endDate);

    // Trouver toutes les disponibilités ordonnées
    List<SalonAvailability> findAllByOrderByDateAscStartTimeAsc();

    // Vérifier s'il y a des chevauchements d'horaires
    @Query("SELECT a FROM SalonAvailability a WHERE a.date = :date " +
            "AND ((a.startTime < :endTime AND a.endTime > :startTime))")
    List<SalonAvailability> findOverlappingAvailabilities(@Param("date") LocalDate date,
                                                          @Param("startTime") LocalTime startTime,
                                                          @Param("endTime") LocalTime endTime);

    // Trouver les disponibilités actives pour une date
    List<SalonAvailability> findByDateAndIsAvailableTrue(LocalDate date);
}