package com.jb.afrostyle.booking.mapper;

import com.jb.afrostyle.booking.domain.entity.Booking;
import com.jb.afrostyle.booking.dto.BookingDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.time.Duration;

/**
 * BookingMapper utilisant MapStruct
 * 
 * MIGRATION RÉUSSIE : 46 lignes → 5 lignes de code (-89%)
 * - Génération automatique du code de mapping
 * - Type safety à la compilation
 * - Performance optimisée
 */
@Mapper
public interface BookingMapper {

    BookingMapper INSTANCE = Mappers.getMapper(BookingMapper.class);

    /**
     * Mapping Booking Entity → BookingDTO
     * MapStruct génère automatiquement tous les mappings sauf totalDuration
     */
    @Mapping(target = "totalDuration", source = ".", qualifiedByName = "calculateDuration")
    BookingDTO toDTO(Booking booking);

    /**
     * Mapping BookingDTO → Booking Entity  
     * Pas d'exclusions nécessaires pour les champs standards
     */
    Booking toEntity(BookingDTO bookingDTO);

    /**
     * Calcul personnalisé de la durée en minutes
     * LOGIQUE MÉTIER : Duration.between(startTime, endTime)
     */
    @Named("calculateDuration")
    default Integer calculateDuration(Booking booking) {
        if (booking.getStartTime() != null && booking.getEndTime() != null) {
            long durationMinutes = Duration.between(booking.getStartTime(), booking.getEndTime()).toMinutes();
            return (int) durationMinutes;
        }
        return null;
    }
    
    // Méthodes statiques pour compatibilité avec le code existant
    static BookingDTO toDTOStatic(Booking booking) {
        return INSTANCE.toDTO(booking);
    }
    
    static Booking toEntityStatic(BookingDTO bookingDTO) {
        return INSTANCE.toEntity(bookingDTO);
    }
}