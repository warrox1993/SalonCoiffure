package com.jb.afrostyle.core.mapper;

import com.jb.afrostyle.core.constants.BusinessConstants;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

/**
 * Utilitaires MapStruct pour la conversion des dates et heures
 * Centralise toutes les conversions temporelles communes
 * Intègre avec BusinessConstants pour les formats standards
 * 
 * @version 1.0
 * @since Java 21
 */
@Mapper(componentModel = "spring")
@Component
public class DateTimeMapperUtils {
    
    // ==================== CONSTANTES DE FORMAT ====================
    
    public static final DateTimeFormatter ISO_LOCAL_DATE_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    public static final DateTimeFormatter ISO_LOCAL_DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    public static final DateTimeFormatter ISO_LOCAL_TIME = DateTimeFormatter.ISO_LOCAL_TIME;
    public static final DateTimeFormatter DISPLAY_DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    public static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("HH:mm");
    
    // ==================== CONVERSIONS LOCALDATETIME ====================
    
    /**
     * Convertit LocalDateTime en String ISO
     * @param dateTime LocalDateTime à convertir
     * @return String au format ISO ou null
     */
    @Named("localDateTimeToString")
    public String localDateTimeToString(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(ISO_LOCAL_DATE_TIME) : null;
    }
    
    /**
     * Convertit String ISO en LocalDateTime
     * @param dateTimeString String au format ISO
     * @return LocalDateTime ou null
     */
    @Named("stringToLocalDateTime")
    public LocalDateTime stringToLocalDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
            return null;
        }
        
        try {
            return LocalDateTime.parse(dateTimeString.trim(), ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            // Tentative avec format d'affichage
            try {
                return LocalDateTime.parse(dateTimeString.trim(), DISPLAY_DATE_TIME);
            } catch (DateTimeParseException ex) {
                return null; // Ignore les formats invalides
            }
        }
    }
    
    /**
     * Convertit LocalDateTime en String d'affichage
     * @param dateTime LocalDateTime à convertir
     * @return String formatée pour affichage
     */
    @Named("localDateTimeToDisplayString")
    public String localDateTimeToDisplayString(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DISPLAY_DATE_TIME) : null;
    }
    
    // ==================== CONVERSIONS LOCALDATE ====================
    
    /**
     * Convertit LocalDate en String ISO
     * @param date LocalDate à convertir
     * @return String au format ISO ou null
     */
    @Named("localDateToString")
    public String localDateToString(LocalDate date) {
        return date != null ? date.format(ISO_LOCAL_DATE) : null;
    }
    
    /**
     * Convertit String ISO en LocalDate
     * @param dateString String au format ISO
     * @return LocalDate ou null
     */
    @Named("stringToLocalDate")
    public LocalDate stringToLocalDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        
        try {
            return LocalDate.parse(dateString.trim(), ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            // Tentative avec format d'affichage
            try {
                return LocalDate.parse(dateString.trim(), DISPLAY_DATE);
            } catch (DateTimeParseException ex) {
                return null; // Ignore les formats invalides
            }
        }
    }
    
    /**
     * Convertit LocalDate en String d'affichage
     * @param date LocalDate à convertir
     * @return String formatée pour affichage
     */
    @Named("localDateToDisplayString")
    public String localDateToDisplayString(LocalDate date) {
        return date != null ? date.format(DISPLAY_DATE) : null;
    }
    
    // ==================== CONVERSIONS LOCALTIME ====================
    
    /**
     * Convertit LocalTime en String ISO
     * @param time LocalTime à convertir
     * @return String au format ISO ou null
     */
    @Named("localTimeToString")
    public String localTimeToString(LocalTime time) {
        return time != null ? time.format(ISO_LOCAL_TIME) : null;
    }
    
    /**
     * Convertit String ISO en LocalTime
     * @param timeString String au format ISO
     * @return LocalTime ou null
     */
    @Named("stringToLocalTime")
    public LocalTime stringToLocalTime(String timeString) {
        if (timeString == null || timeString.trim().isEmpty()) {
            return null;
        }
        
        try {
            return LocalTime.parse(timeString.trim(), ISO_LOCAL_TIME);
        } catch (DateTimeParseException e) {
            // Tentative avec format d'affichage
            try {
                return LocalTime.parse(timeString.trim(), DISPLAY_TIME);
            } catch (DateTimeParseException ex) {
                return null; // Ignore les formats invalides
            }
        }
    }
    
    /**
     * Convertit LocalTime en String d'affichage
     * @param time LocalTime à convertir
     * @return String formatée pour affichage
     */
    @Named("localTimeToDisplayString")
    public String localTimeToDisplayString(LocalTime time) {
        return time != null ? time.format(DISPLAY_TIME) : null;
    }
    
    // ==================== CONVERSIONS DURATION ====================
    
    /**
     * Convertit Duration en minutes (Integer)
     * @param duration Duration à convertir
     * @return Nombre de minutes ou null
     */
    @Named("durationToMinutes")
    public Integer durationToMinutes(Duration duration) {
        return duration != null ? (int) duration.toMinutes() : null;
    }
    
    /**
     * Convertit minutes (Integer) en Duration
     * @param minutes Nombre de minutes
     * @return Duration ou null
     */
    @Named("minutesToDuration")
    public Duration minutesToDuration(Integer minutes) {
        return minutes != null ? Duration.ofMinutes(minutes) : null;
    }
    
    /**
     * Calcule la durée entre deux LocalDateTime
     * @param start Heure de début
     * @param end Heure de fin
     * @return Durée en minutes ou null
     */
    @Named("calculateDurationMinutes")
    public Integer calculateDurationMinutes(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return null;
        }
        
        Duration duration = Duration.between(start, end);
        return (int) duration.toMinutes();
    }
    
    /**
     * Calcule la durée entre deux LocalTime
     * @param start Heure de début
     * @param end Heure de fin
     * @return Durée en minutes ou null
     */
    @Named("calculateTimeRangeMinutes")
    public Integer calculateTimeRangeMinutes(LocalTime start, LocalTime end) {
        if (start == null || end == null) {
            return null;
        }
        
        Duration duration = Duration.between(start, end);
        return (int) duration.toMinutes();
    }
    
    // ==================== VALIDATIONS MÉTIER ====================
    
    /**
     * Vérifie si une heure est dans les heures d'ouverture
     * @param time Heure à vérifier
     * @return true si dans les heures d'ouverture
     */
    @Named("isInBusinessHours")
    public Boolean isInBusinessHours(LocalTime time) {
        if (time == null) {
            return null;
        }
        
        return !time.isBefore(BusinessConstants.BUSINESS_OPEN_TIME) && 
               !time.isAfter(BusinessConstants.BUSINESS_CLOSE_TIME);
    }
    
    /**
     * Vérifie si une date est dans le futur
     * @param dateTime Date à vérifier
     * @return true si dans le futur
     */
    @Named("isFutureDateTime")
    public Boolean isFutureDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        
        return dateTime.isAfter(LocalDateTime.now());
    }
    
    /**
     * Vérifie si une date est dans le passé
     * @param dateTime Date à vérifier
     * @return true si dans le passé
     */
    @Named("isPastDateTime")
    public Boolean isPastDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        
        return dateTime.isBefore(LocalDateTime.now());
    }
    
    /**
     * Arrondit une LocalDateTime aux minutes près
     * @param dateTime DateTime à arrondir
     * @return DateTime arrondie
     */
    @Named("roundToMinutes")
    public LocalDateTime roundToMinutes(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        
        return dateTime.withSecond(0).withNano(0);
    }
    
    // ==================== UTILITAIRES TIMEZONE ====================
    
    /**
     * Convertit LocalDateTime en Instant (UTC)
     * @param dateTime LocalDateTime à convertir
     * @return Instant ou null
     */
    @Named("localDateTimeToInstant")
    public Instant localDateTimeToInstant(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        
        return dateTime.atZone(ZoneId.systemDefault()).toInstant();
    }
    
    /**
     * Convertit Instant en LocalDateTime (timezone système)
     * @param instant Instant à convertir
     * @return LocalDateTime ou null
     */
    @Named("instantToLocalDateTime")
    public LocalDateTime instantToLocalDateTime(Instant instant) {
        if (instant == null) {
            return null;
        }
        
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
    
    // ==================== FORMATAGE PERSONNALISÉ ====================
    
    /**
     * Formate une durée en texte lisible
     * @param duration Duration à formater
     * @return String lisible (ex: "2h 30min")
     */
    @Named("formatDurationToText")
    public String formatDurationToText(Duration duration) {
        if (duration == null) {
            return null;
        }
        
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        
        StringBuilder sb = new StringBuilder();
        if (hours > 0) {
            sb.append(hours).append("h");
        }
        if (minutes > 0) {
            if (hours > 0) sb.append(" ");
            sb.append(minutes).append("min");
        }
        
        return sb.length() > 0 ? sb.toString() : "0min";
    }
    
    /**
     * Formate une durée en minutes en texte lisible
     * @param minutes Nombre de minutes
     * @return String lisible (ex: "2h 30min")
     */
    @Named("formatMinutesToText")
    public String formatMinutesToText(Integer minutes) {
        if (minutes == null) {
            return null;
        }
        
        return formatDurationToText(Duration.ofMinutes(minutes));
    }
}