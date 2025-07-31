package com.jb.afrostyle.core.util;

import com.jb.afrostyle.core.constants.BusinessConstants;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.IntStream;

/**
 * Utilitaires de date et heure pour la logique métier AfroStyle
 * Complète DateTimeMapperUtils en se concentrant sur les règles métier
 * Intègre avec BusinessConstants pour les horaires et règles de réservation
 * 
 * @version 1.0
 * @since Java 21
 */
public final class DateTimeUtils {
    
    // ==================== CONSTANTES ====================
    
    // Formats de date/heure localisés
    public static final DateTimeFormatter FRENCH_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter FRENCH_DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    public static final DateTimeFormatter ISO_DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    public static final DateTimeFormatter ISO_DATETIME_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    // Timezone par défaut pour l'application (Europe/Brussels)
    public static final ZoneId DEFAULT_ZONE = ZoneId.of("Europe/Brussels");
    
    /**
     * Constructeur privé pour classe utilitaire
     */
    private DateTimeUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    // ==================== OBTENTION DE DATES COURANTES ====================
    
    /**
     * Obtient la date/heure actuelle dans la timezone par défaut
     * @return LocalDateTime actuel
     */
    public static LocalDateTime now() {
        return LocalDateTime.now(DEFAULT_ZONE);
    }
    
    /**
     * Obtient la date actuelle dans la timezone par défaut
     * @return LocalDate actuel
     */
    public static LocalDate today() {
        return LocalDate.now(DEFAULT_ZONE);
    }
    
    /**
     * Obtient l'heure actuelle dans la timezone par défaut
     * @return LocalTime actuel
     */
    public static LocalTime currentTime() {
        return LocalTime.now(DEFAULT_ZONE);
    }
    
    /**
     * Obtient la date de demain
     * @return LocalDate de demain
     */
    public static LocalDate tomorrow() {
        return today().plusDays(1);
    }
    
    /**
     * Obtient la date d'hier
     * @return LocalDate d'hier
     */
    public static LocalDate yesterday() {
        return today().minusDays(1);
    }
    
    // ==================== VALIDATION DES HEURES D'OUVERTURE ====================
    
    /**
     * Vérifie si une heure est dans les heures d'ouverture
     * @param time Heure à vérifier
     * @return true si dans les heures d'ouverture
     */
    public static boolean isWithinBusinessHours(LocalTime time) {
        if (time == null) {
            return false;
        }
        
        return !time.isBefore(BusinessConstants.BUSINESS_OPEN_TIME) && 
               !time.isAfter(BusinessConstants.BUSINESS_CLOSE_TIME);
    }
    
    /**
     * Vérifie si une date/heure est dans les heures d'ouverture
     * @param dateTime Date/heure à vérifier
     * @return true si dans les heures d'ouverture
     */
    public static boolean isWithinBusinessHours(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        
        return isWithinBusinessHours(dateTime.toLocalTime());
    }
    
    /**
     * Vérifie si une date est un jour ouvrable (lundi à samedi)
     * @param date Date à vérifier
     * @return true si jour ouvrable
     */
    public static boolean isBusinessDay(LocalDate date) {
        if (date == null) {
            return false;
        }
        
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SUNDAY; // Ouvert du lundi au samedi
    }
    
    /**
     * Vérifie si une date/heure est dans une période ouvrable complète
     * @param dateTime Date/heure à vérifier
     * @return true si période ouvrable
     */
    public static boolean isBusinessDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        
        return isBusinessDay(dateTime.toLocalDate()) && isWithinBusinessHours(dateTime.toLocalTime());
    }
    
    // ==================== CALCULS DE RÉSERVATION ====================
    
    /**
     * Vérifie si une réservation peut être faite (délai minimum)
     * @param bookingDateTime Date/heure de la réservation
     * @return true si réservation possible
     */
    public static boolean canMakeBooking(LocalDateTime bookingDateTime) {
        if (bookingDateTime == null) {
            return false;
        }
        
        LocalDateTime minBookingTime = now().plus(BusinessConstants.MIN_BOOKING_ADVANCE);
        return bookingDateTime.isAfter(minBookingTime) && isBusinessDateTime(bookingDateTime);
    }
    
    /**
     * Vérifie si une réservation peut être annulée (délai minimum)
     * @param bookingDateTime Date/heure de la réservation
     * @return true si annulation possible
     */
    public static boolean canCancelBooking(LocalDateTime bookingDateTime) {
        if (bookingDateTime == null) {
            return false;
        }
        
        LocalDateTime minCancelTime = bookingDateTime.minus(BusinessConstants.MIN_CANCELLATION_NOTICE);
        return now().isBefore(minCancelTime);
    }
    
    /**
     * Calcule la prochaine heure d'ouverture disponible
     * @return LocalDateTime de la prochaine ouverture
     */
    public static LocalDateTime getNextBusinessOpen() {
        LocalDateTime now = now();
        LocalDate currentDate = now.toLocalDate();
        
        // Si nous sommes dans les heures d'ouverture aujourd'hui
        if (isBusinessDay(currentDate) && now.toLocalTime().isBefore(BusinessConstants.BUSINESS_CLOSE_TIME)) {
            if (now.toLocalTime().isBefore(BusinessConstants.BUSINESS_OPEN_TIME)) {
                return currentDate.atTime(BusinessConstants.BUSINESS_OPEN_TIME);
            } else {
                return now; // Déjà ouvert
            }
        }
        
        // Chercher le prochain jour ouvrable
        LocalDate nextBusinessDay = currentDate.plusDays(1);
        while (!isBusinessDay(nextBusinessDay)) {
            nextBusinessDay = nextBusinessDay.plusDays(1);
        }
        
        return nextBusinessDay.atTime(BusinessConstants.BUSINESS_OPEN_TIME);
    }
    
    /**
     * Génère les créneaux horaires disponibles pour une date
     * @param date Date pour laquelle générer les créneaux
     * @param durationMinutes Durée du service en minutes
     * @return Liste des créneaux disponibles
     */
    public static List<LocalTime> generateTimeSlots(LocalDate date, int durationMinutes) {
        List<LocalTime> slots = new ArrayList<>();
        
        if (!isBusinessDay(date) || durationMinutes <= 0) {
            return slots;
        }
        
        LocalTime currentSlot = BusinessConstants.BUSINESS_OPEN_TIME;
        LocalTime lastPossibleStart = BusinessConstants.BUSINESS_CLOSE_TIME.minusMinutes(durationMinutes);
        
        while (!currentSlot.isAfter(lastPossibleStart)) {
            slots.add(currentSlot);
            currentSlot = currentSlot.plusMinutes(BusinessConstants.SLOT_INTERVAL_MINUTES);
        }
        
        return slots;
    }
    
    // ==================== FORMATAGE SPÉCIALISÉ ====================
    
    /**
     * Formate une date au format français (dd/MM/yyyy)
     * @param date Date à formater
     * @return String formatée
     */
    public static String formatFrenchDate(LocalDate date) {
        return date != null ? date.format(FRENCH_DATE_FORMAT) : null;
    }
    
    /**
     * Formate une date/heure au format français (dd/MM/yyyy HH:mm)
     * @param dateTime Date/heure à formater
     * @return String formatée
     */
    public static String formatFrenchDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(FRENCH_DATETIME_FORMAT) : null;
    }
    
    /**
     * Formate une heure (HH:mm)
     * @param time Heure à formater
     * @return String formatée
     */
    public static String formatTime(LocalTime time) {
        return time != null ? time.format(TIME_FORMAT) : null;
    }
    
    /**
     * Formate une durée en texte lisible
     * @param duration Durée à formater
     * @return String lisible (ex: "2h 30min")
     */
    public static String formatDuration(Duration duration) {
        if (duration == null) {
            return null;
        }
        
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        
        if (hours == 0) {
            return minutes + "min";
        } else if (minutes == 0) {
            return hours + "h";
        } else {
            return hours + "h " + minutes + "min";
        }
    }
    
    /**
     * Formate un créneau horaire (ex: "14:00 - 15:30")
     * @param startTime Heure de début
     * @param endTime Heure de fin
     * @return String formatée
     */
    public static String formatTimeSlot(LocalTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null) {
            return null;
        }
        
        return formatTime(startTime) + " - " + formatTime(endTime);
    }
    
    /**
     * Formate une période (ex: "Lundi 15/01/2024 de 14:00 à 15:30")
     * @param dateTime Date/heure de début
     * @param duration Durée
     * @return String formatée
     */
    public static String formatBookingPeriod(LocalDateTime dateTime, Duration duration) {
        if (dateTime == null || duration == null) {
            return null;
        }
        
        LocalDateTime endDateTime = dateTime.plus(duration);
        String dayName = getDayNameInFrench(dateTime.getDayOfWeek());
        
        return String.format("%s %s de %s à %s",
                           dayName,
                           formatFrenchDate(dateTime.toLocalDate()),
                           formatTime(dateTime.toLocalTime()),
                           formatTime(endDateTime.toLocalTime()));
    }
    
    // ==================== CALCULS DE PÉRIODES ====================
    
    /**
     * Calcule l'âge en années
     * @param birthDate Date de naissance
     * @return Âge en années
     */
    public static int calculateAge(LocalDate birthDate) {
        if (birthDate == null) {
            return 0;
        }
        
        return Period.between(birthDate, today()).getYears();
    }
    
    /**
     * Calcule le nombre de jours ouvrables entre deux dates
     * @param startDate Date de début
     * @param endDate Date de fin
     * @return Nombre de jours ouvrables
     */
    public static long calculateBusinessDays(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            return 0;
        }
        
        return startDate.datesUntil(endDate.plusDays(1))
                       .filter(DateTimeUtils::isBusinessDay)
                       .count();
    }
    
    /**
     * Obtient le début de la semaine (lundi)
     * @param date Date de référence
     * @return Début de semaine
     */
    public static LocalDate getStartOfWeek(LocalDate date) {
        if (date == null) {
            return null;
        }
        
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }
    
    /**
     * Obtient la fin de la semaine (dimanche)
     * @param date Date de référence
     * @return Fin de semaine
     */
    public static LocalDate getEndOfWeek(LocalDate date) {
        if (date == null) {
            return null;
        }
        
        return date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    }
    
    /**
     * Obtient le début du mois
     * @param date Date de référence
     * @return Début du mois
     */
    public static LocalDate getStartOfMonth(LocalDate date) {
        if (date == null) {
            return null;
        }
        
        return date.with(TemporalAdjusters.firstDayOfMonth());
    }
    
    /**
     * Obtient la fin du mois
     * @param date Date de référence
     * @return Fin du mois
     */
    public static LocalDate getEndOfMonth(LocalDate date) {
        if (date == null) {
            return null;
        }
        
        return date.with(TemporalAdjusters.lastDayOfMonth());
    }
    
    // ==================== GÉNÉRATION DE DATES ====================
    
    /**
     * Génère les dates d'une semaine
     * @param startOfWeek Début de semaine
     * @return Liste des 7 dates de la semaine
     */
    public static List<LocalDate> generateWeekDates(LocalDate startOfWeek) {
        if (startOfWeek == null) {
            return List.of();
        }
        
        return IntStream.range(0, 7)
                       .mapToObj(startOfWeek::plusDays)
                       .toList();
    }
    
    /**
     * Génère les dates d'un mois
     * @param yearMonth Année et mois
     * @return Liste des dates du mois
     */
    public static List<LocalDate> generateMonthDates(YearMonth yearMonth) {
        if (yearMonth == null) {
            return List.of();
        }
        
        LocalDate firstDay = yearMonth.atDay(1);
        int daysInMonth = yearMonth.lengthOfMonth();
        
        return IntStream.range(0, daysInMonth)
                       .mapToObj(firstDay::plusDays)
                       .toList();
    }
    
    /**
     * Génère les prochains jours ouvrables
     * @param numberOfDays Nombre de jours ouvrables à générer
     * @return Liste des prochains jours ouvrables
     */
    public static List<LocalDate> generateNextBusinessDays(int numberOfDays) {
        List<LocalDate> businessDays = new ArrayList<>();
        LocalDate currentDate = today();
        
        while (businessDays.size() < numberOfDays) {
            if (isBusinessDay(currentDate)) {
                businessDays.add(currentDate);
            }
            currentDate = currentDate.plusDays(1);
        }
        
        return businessDays;
    }
    
    // ==================== VALIDATION DE DATES ====================
    
    /**
     * Vérifie si une date est dans le futur
     * @param date Date à vérifier
     * @return true si dans le futur
     */
    public static boolean isFuture(LocalDate date) {
        return date != null && date.isAfter(today());
    }
    
    /**
     * Vérifie si une date/heure est dans le futur
     * @param dateTime Date/heure à vérifier
     * @return true si dans le futur
     */
    public static boolean isFuture(LocalDateTime dateTime) {
        return dateTime != null && dateTime.isAfter(now());
    }
    
    /**
     * Vérifie si une date est dans le passé
     * @param date Date à vérifier
     * @return true si dans le passé
     */
    public static boolean isPast(LocalDate date) {
        return date != null && date.isBefore(today());
    }
    
    /**
     * Vérifie si une date/heure est dans le passé
     * @param dateTime Date/heure à vérifier
     * @return true si dans le passé
     */
    public static boolean isPast(LocalDateTime dateTime) {
        return dateTime != null && dateTime.isBefore(now());
    }
    
    /**
     * Vérifie si une date est aujourd'hui
     * @param date Date à vérifier
     * @return true si aujourd'hui
     */
    public static boolean isToday(LocalDate date) {
        return date != null && date.equals(today());
    }
    
    /**
     * Vérifie si une date est dans la semaine courante
     * @param date Date à vérifier
     * @return true si semaine courante
     */
    public static boolean isThisWeek(LocalDate date) {
        if (date == null) {
            return false;
        }
        
        LocalDate startOfWeek = getStartOfWeek(today());
        LocalDate endOfWeek = getEndOfWeek(today());
        
        return !date.isBefore(startOfWeek) && !date.isAfter(endOfWeek);
    }
    
    /**
     * Vérifie si une date est dans le mois courant
     * @param date Date à vérifier
     * @return true si mois courant
     */
    public static boolean isThisMonth(LocalDate date) {
        if (date == null) {
            return false;
        }
        
        LocalDate today = today();
        return date.getYear() == today.getYear() && date.getMonth() == today.getMonth();
    }
    
    // ==================== CONVERSION ET PARSING ====================
    
    /**
     * Parse une date au format français (dd/MM/yyyy)
     * @param dateStr Chaîne de date
     * @return LocalDate ou null si invalide
     */
    public static LocalDate parseFrenchDate(String dateStr) {
        if (StringUtils.isBlank(dateStr)) {
            return null;
        }
        
        try {
            return LocalDate.parse(dateStr.trim(), FRENCH_DATE_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
    
    /**
     * Parse une date/heure au format français (dd/MM/yyyy HH:mm)
     * @param dateTimeStr Chaîne de date/heure
     * @return LocalDateTime ou null si invalide
     */
    public static LocalDateTime parseFrenchDateTime(String dateTimeStr) {
        if (StringUtils.isBlank(dateTimeStr)) {
            return null;
        }
        
        try {
            return LocalDateTime.parse(dateTimeStr.trim(), FRENCH_DATETIME_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
    
    /**
     * Parse une heure (HH:mm)
     * @param timeStr Chaîne d'heure
     * @return LocalTime ou null si invalide
     */
    public static LocalTime parseTime(String timeStr) {
        if (StringUtils.isBlank(timeStr)) {
            return null;
        }
        
        try {
            return LocalTime.parse(timeStr.trim(), TIME_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
    
    /**
     * Convertit une LocalDateTime en Instant (timezone par défaut)
     * @param dateTime LocalDateTime à convertir
     * @return Instant correspondant
     */
    public static Instant toInstant(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.atZone(DEFAULT_ZONE).toInstant() : null;
    }
    
    /**
     * Convertit un Instant en LocalDateTime (timezone par défaut)
     * @param instant Instant à convertir
     * @return LocalDateTime correspondant
     */
    public static LocalDateTime fromInstant(Instant instant) {
        return instant != null ? LocalDateTime.ofInstant(instant, DEFAULT_ZONE) : null;
    }
    
    // ==================== UTILITAIRES TEXTUELS ====================
    
    /**
     * Obtient le nom du jour en français
     * @param dayOfWeek Jour de la semaine
     * @return Nom en français
     */
    public static String getDayNameInFrench(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "Lundi";
            case TUESDAY -> "Mardi";
            case WEDNESDAY -> "Mercredi";
            case THURSDAY -> "Jeudi";
            case FRIDAY -> "Vendredi";
            case SATURDAY -> "Samedi";
            case SUNDAY -> "Dimanche";
        };
    }
    
    /**
     * Obtient le nom du mois en français
     * @param month Mois
     * @return Nom en français
     */
    public static String getMonthNameInFrench(Month month) {
        return switch (month) {
            case JANUARY -> "Janvier";
            case FEBRUARY -> "Février";
            case MARCH -> "Mars";
            case APRIL -> "Avril";
            case MAY -> "Mai";
            case JUNE -> "Juin";
            case JULY -> "Juillet";
            case AUGUST -> "Août";
            case SEPTEMBER -> "Septembre";
            case OCTOBER -> "Octobre";
            case NOVEMBER -> "Novembre";
            case DECEMBER -> "Décembre";
        };
    }
    
    /**
     * Génère un texte relatif pour une date (ex: "il y a 2 heures")
     * @param dateTime Date/heure de référence
     * @return Texte relatif
     */
    public static String getRelativeTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        
        LocalDateTime now = now();
        Duration duration = Duration.between(dateTime, now);
        
        if (duration.isNegative()) {
            duration = duration.abs();
            long minutes = duration.toMinutes();
            long hours = duration.toHours();
            long days = duration.toDays();
            
            if (minutes < 60) {
                return minutes == 0 ? "maintenant" : "dans " + minutes + " minute" + (minutes > 1 ? "s" : "");
            } else if (hours < 24) {
                return "dans " + hours + " heure" + (hours > 1 ? "s" : "");
            } else {
                return "dans " + days + " jour" + (days > 1 ? "s" : "");
            }
        } else {
            long minutes = duration.toMinutes();
            long hours = duration.toHours();
            long days = duration.toDays();
            
            if (minutes < 60) {
                return minutes == 0 ? "maintenant" : "il y a " + minutes + " minute" + (minutes > 1 ? "s" : "");
            } else if (hours < 24) {
                return "il y a " + hours + " heure" + (hours > 1 ? "s" : "");
            } else if (days < 30) {
                return "il y a " + days + " jour" + (days > 1 ? "s" : "");
            } else {
                return formatFrenchDate(dateTime.toLocalDate());
            }
        }
    }
}