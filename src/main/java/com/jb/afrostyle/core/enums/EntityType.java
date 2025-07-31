package com.jb.afrostyle.core.enums;

/**
 * Types d'entités métier pour le système AfroStyle
 * Utilisé pour la gestion centralisée des exceptions, validations et logging
 * 
 * @version 1.0
 * @since Java 21
 */
public enum EntityType {
    
    // ==================== ENTITÉS UTILISATEUR ====================
    
    /** Entité utilisateur */
    USER("User", "utilisateur", "users"),
    
    /** Token de réinitialisation de mot de passe */
    PASSWORD_RESET_TOKEN("PasswordResetToken", "token de réinitialisation", "password-reset-tokens"),
    
    // ==================== ENTITÉS SALON ====================
    
    /** Entité salon */
    SALON("Salon", "salon", "salons"),
    
    /** Paramètres du salon */
    SALON_SETTINGS("SalonSettings", "paramètres du salon", "salon-settings"),
    
    /** Rapport de salon */
    SALON_REPORT("SalonReport", "rapport de salon", "salon-reports"),
    
    // ==================== ENTITÉS SERVICE ====================
    
    /** Offre de service */
    SERVICE_OFFERING("ServiceOffering", "service", "services"),
    
    /** Catégorie de service */
    CATEGORY("Category", "catégorie", "categories"),
    
    // ==================== ENTITÉS RÉSERVATION ====================
    
    /** Réservation */
    BOOKING("Booking", "réservation", "bookings"),
    
    /** Disponibilité du salon */
    SALON_AVAILABILITY("SalonAvailability", "disponibilité", "availabilities"),
    
    /** Créneau horaire */
    TIME_SLOT("TimeSlot", "créneau horaire", "time-slots"),
    
    // ==================== ENTITÉS PAIEMENT ====================
    
    /** Paiement */
    PAYMENT("Payment", "paiement", "payments"),
    
    /** Session de paiement Stripe */
    STRIPE_SESSION("StripeSession", "session Stripe", "stripe-sessions"),
    
    /** Remboursement */
    REFUND("Refund", "remboursement", "refunds"),
    
    // ==================== ENTITÉS NOTIFICATION ====================
    
    /** File d'attente des notifications */
    NOTIFICATION_QUEUE("NotificationQueue", "notification", "notifications"),
    
    /** Préférence de notification */
    NOTIFICATION_PREFERENCE("NotificationPreference", "préférence de notification", "notification-preferences"),
    
    // ==================== ENTITÉS SYSTÈME ====================
    
    /** Session utilisateur */
    USER_SESSION("UserSession", "session utilisateur", "user-sessions"),
    
    /** Token d'authentification */
    AUTH_TOKEN("AuthToken", "token d'authentification", "auth-tokens"),
    
    /** Log d'audit */
    AUDIT_LOG("AuditLog", "log d'audit", "audit-logs"),
    
    // ==================== ENTITÉS INTÉGRATION ====================
    
    /** Événement webhook */
    WEBHOOK_EVENT("WebhookEvent", "événement webhook", "webhook-events"),
    
    /** Calendrier Google */
    GOOGLE_CALENDAR_EVENT("GoogleCalendarEvent", "événement Google Calendar", "google-calendar-events"),
    
    /** Email */
    EMAIL("Email", "email", "emails");
    
    // ==================== PROPRIÉTÉS ====================
    
    private final String className;
    private final String displayName;
    private final String pluralName;
    
    /**
     * Constructeur de l'enum EntityType
     * 
     * @param className Nom de la classe Java
     * @param displayName Nom d'affichage en français
     * @param pluralName Nom au pluriel pour les URLs REST
     */
    EntityType(String className, String displayName, String pluralName) {
        this.className = className;
        this.displayName = displayName;
        this.pluralName = pluralName;
    }
    
    // ==================== GETTERS ====================
    
    /**
     * Obtient le nom de la classe Java
     * @return Nom de la classe
     */
    public String getClassName() {
        return className;
    }
    
    /**
     * Obtient le nom d'affichage en français
     * @return Nom d'affichage
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Obtient le nom au pluriel pour les URLs REST
     * @return Nom au pluriel
     */
    public String getPluralName() {
        return pluralName;
    }
    
    // ==================== MÉTHODES UTILITAIRES ====================
    
    /**
     * Trouve un EntityType par nom de classe
     * @param className Nom de la classe à chercher
     * @return EntityType correspondant ou null si non trouvé
     */
    public static EntityType fromClassName(String className) {
        for (EntityType type : values()) {
            if (type.className.equalsIgnoreCase(className)) {
                return type;
            }
        }
        return null;
    }
    
    /**
     * Trouve un EntityType par nom au pluriel
     * @param pluralName Nom au pluriel à chercher
     * @return EntityType correspondant ou null si non trouvé
     */
    public static EntityType fromPluralName(String pluralName) {
        for (EntityType type : values()) {
            if (type.pluralName.equalsIgnoreCase(pluralName)) {
                return type;
            }
        }
        return null;
    }
    
    /**
     * Vérifie si l'entité est liée aux utilisateurs
     * @return true si c'est une entité utilisateur
     */
    public boolean isUserEntity() {
        return this == USER || this == PASSWORD_RESET_TOKEN || this == USER_SESSION || this == AUTH_TOKEN;
    }
    
    /**
     * Vérifie si l'entité est liée aux salons
     * @return true si c'est une entité salon
     */
    public boolean isSalonEntity() {
        return this == SALON || this == SALON_SETTINGS || this == SALON_REPORT || this == SALON_AVAILABILITY;
    }
    
    /**
     * Vérifie si l'entité est liée aux services
     * @return true si c'est une entité service
     */
    public boolean isServiceEntity() {
        return this == SERVICE_OFFERING || this == CATEGORY;
    }
    
    /**
     * Vérifie si l'entité est liée aux réservations
     * @return true si c'est une entité réservation
     */
    public boolean isBookingEntity() {
        return this == BOOKING || this == SALON_AVAILABILITY || this == TIME_SLOT;
    }
    
    /**
     * Vérifie si l'entité est liée aux paiements
     * @return true si c'est une entité paiement
     */
    public boolean isPaymentEntity() {
        return this == PAYMENT || this == STRIPE_SESSION || this == REFUND;
    }
    
    /**
     * Vérifie si l'entité est liée aux notifications
     * @return true si c'est une entité notification
     */
    public boolean isNotificationEntity() {
        return this == NOTIFICATION_QUEUE || this == NOTIFICATION_PREFERENCE;
    }
    
    /**
     * Vérifie si l'entité est liée au système
     * @return true si c'est une entité système
     */
    public boolean isSystemEntity() {
        return this == USER_SESSION || this == AUTH_TOKEN || this == AUDIT_LOG;
    }
    
    /**
     * Vérifie si l'entité est liée aux intégrations
     * @return true si c'est une entité intégration
     */
    public boolean isIntegrationEntity() {
        return this == WEBHOOK_EVENT || this == GOOGLE_CALENDAR_EVENT || this == EMAIL;
    }
    
    /**
     * Génère l'URL REST pour cette entité
     * @return URL REST de base (ex: "/api/users")
     */
    public String getRestUrl() {
        return "/api/" + pluralName;
    }
    
    /**
     * Génère l'URL REST pour une instance spécifique
     * @param id Identifiant de l'instance
     * @return URL REST complète (ex: "/api/users/123")
     */
    public String getRestUrl(Object id) {
        return getRestUrl() + "/" + id;
    }
    
    /**
     * Génère un message d'erreur standardisé pour cette entité
     * @param operation Opération échouée (CREATE, READ, UPDATE, DELETE)
     * @param identifier Identifiant de l'entité (optionnel)
     * @return Message d'erreur formaté
     */
    public String getErrorMessage(String operation, Object identifier) {
        String baseMessage = switch (operation.toUpperCase()) {
            case "CREATE" -> "Impossible de créer " + displayName;
            case "READ", "FIND" -> displayName + " introuvable";
            case "UPDATE" -> "Impossible de modifier " + displayName;
            case "DELETE" -> "Impossible de supprimer " + displayName;
            default -> "Erreur avec " + displayName;
        };
        
        if (identifier != null) {
            baseMessage += " (ID: " + identifier + ")";
        }
        
        return baseMessage;
    }
    
    /**
     * Génère un message de succès standardisé pour cette entité
     * @param operation Opération réussie
     * @param identifier Identifiant de l'entité (optionnel)
     * @return Message de succès formaté
     */
    public String getSuccessMessage(String operation, Object identifier) {
        String baseMessage = switch (operation.toUpperCase()) {
            case "CREATE" -> displayName + " créé avec succès";
            case "UPDATE" -> displayName + " modifié avec succès";
            case "DELETE" -> displayName + " supprimé avec succès";
            default -> "Opération sur " + displayName + " réussie";
        };
        
        if (identifier != null) {
            baseMessage += " (ID: " + identifier + ")";
        }
        
        return baseMessage;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}