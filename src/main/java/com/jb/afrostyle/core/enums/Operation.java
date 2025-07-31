package com.jb.afrostyle.core.enums;

import org.springframework.http.HttpStatus;

/**
 * Types d'opérations CRUD et métier pour le système AfroStyle
 * Utilisé pour la gestion centralisée des réponses HTTP, audit et logging
 * 
 * @version 1.0
 * @since Java 21
 */
public enum Operation {
    
    // ==================== OPÉRATIONS CRUD DE BASE ====================
    
    /** Création d'une nouvelle entité */
    CREATE("Create", "créer", "création", HttpStatus.CREATED, true),
    
    /** Lecture/consultation d'une entité */
    READ("Read", "consulter", "consultation", HttpStatus.OK, false),
    
    /** Mise à jour d'une entité existante */
    UPDATE("Update", "modifier", "modification", HttpStatus.OK, true),
    
    /** Suppression d'une entité */
    DELETE("Delete", "supprimer", "suppression", HttpStatus.OK, true),
    
    // ==================== OPÉRATIONS DE RECHERCHE ====================
    
    /** Recherche d'entités avec critères */
    SEARCH("Search", "rechercher", "recherche", HttpStatus.OK, false),
    
    /** Listage d'entités avec pagination */
    LIST("List", "lister", "listage", HttpStatus.OK, false),
    
    /** Comptage d'entités */
    COUNT("Count", "compter", "comptage", HttpStatus.OK, false),
    
    /** Vérification d'existence */
    EXISTS("Exists", "vérifier l'existence", "vérification", HttpStatus.OK, false),
    
    // ==================== OPÉRATIONS MÉTIER UTILISATEUR ====================
    
    /** Connexion utilisateur */
    LOGIN("Login", "se connecter", "connexion", HttpStatus.OK, true),
    
    /** Déconnexion utilisateur */
    LOGOUT("Logout", "se déconnecter", "déconnexion", HttpStatus.OK, true),
    
    /** Inscription utilisateur */
    REGISTER("Register", "s'inscrire", "inscription", HttpStatus.CREATED, true),
    
    /** Changement de mot de passe */
    CHANGE_PASSWORD("ChangePassword", "changer le mot de passe", "changement de mot de passe", HttpStatus.OK, true),
    
    /** Réinitialisation de mot de passe */
    RESET_PASSWORD("ResetPassword", "réinitialiser le mot de passe", "réinitialisation de mot de passe", HttpStatus.OK, true),
    
    /** Vérification email */
    VERIFY_EMAIL("VerifyEmail", "vérifier l'email", "vérification email", HttpStatus.OK, true),
    
    // ==================== OPÉRATIONS MÉTIER RÉSERVATION ====================
    
    /** Création de réservation */
    BOOK("Book", "réserver", "réservation", HttpStatus.CREATED, true),
    
    /** Confirmation de réservation */
    CONFIRM_BOOKING("ConfirmBooking", "confirmer la réservation", "confirmation de réservation", HttpStatus.OK, true),
    
    /** Annulation de réservation */
    CANCEL_BOOKING("CancelBooking", "annuler la réservation", "annulation de réservation", HttpStatus.OK, true),
    
    /** Reprogrammation de réservation */
    RESCHEDULE_BOOKING("RescheduleBooking", "reprogrammer la réservation", "reprogrammation", HttpStatus.OK, true),
    
    /** Vérification de disponibilité */
    CHECK_AVAILABILITY("CheckAvailability", "vérifier la disponibilité", "vérification de disponibilité", HttpStatus.OK, false),
    
    // ==================== OPÉRATIONS MÉTIER PAIEMENT ====================
    
    /** Traitement de paiement */
    PROCESS_PAYMENT("ProcessPayment", "traiter le paiement", "traitement de paiement", HttpStatus.OK, true),
    
    /** Création de session de paiement */
    CREATE_PAYMENT_SESSION("CreatePaymentSession", "créer une session de paiement", "création de session", HttpStatus.CREATED, true),
    
    /** Remboursement */
    REFUND("Refund", "rembourser", "remboursement", HttpStatus.OK, true),
    
    /** Validation de paiement */
    VALIDATE_PAYMENT("ValidatePayment", "valider le paiement", "validation de paiement", HttpStatus.OK, false),
    
    // ==================== OPÉRATIONS MÉTIER SALON ====================
    
    /** Activation de salon */
    ACTIVATE_SALON("ActivateSalon", "activer le salon", "activation de salon", HttpStatus.OK, true),
    
    /** Désactivation de salon */
    DEACTIVATE_SALON("DeactivateSalon", "désactiver le salon", "désactivation de salon", HttpStatus.OK, true),
    
    /** Mise à jour des paramètres */
    UPDATE_SETTINGS("UpdateSettings", "modifier les paramètres", "modification des paramètres", HttpStatus.OK, true),
    
    // ==================== OPÉRATIONS DE NOTIFICATION ====================
    
    /** Envoi de notification */
    SEND_NOTIFICATION("SendNotification", "envoyer une notification", "envoi de notification", HttpStatus.OK, true),
    
    /** Envoi d'email */
    SEND_EMAIL("SendEmail", "envoyer un email", "envoi d'email", HttpStatus.OK, true),
    
    /** Envoi de SMS */
    SEND_SMS("SendSMS", "envoyer un SMS", "envoi de SMS", HttpStatus.OK, true),
    
    // ==================== OPÉRATIONS D'IMPORT/EXPORT ====================
    
    /** Import de données */
    IMPORT("Import", "importer", "import", HttpStatus.OK, true),
    
    /** Export de données */
    EXPORT("Export", "exporter", "export", HttpStatus.OK, false),
    
    /** Sauvegarde */
    BACKUP("Backup", "sauvegarder", "sauvegarde", HttpStatus.OK, true),
    
    /** Restauration */
    RESTORE("Restore", "restaurer", "restauration", HttpStatus.OK, true),
    
    // ==================== OPÉRATIONS DE VALIDATION ====================
    
    /** Validation de données */
    VALIDATE("Validate", "valider", "validation", HttpStatus.OK, false),
    
    /** Vérification de permissions */
    CHECK_PERMISSION("CheckPermission", "vérifier les permissions", "vérification de permissions", HttpStatus.OK, false),
    
    /** Audit de sécurité */
    SECURITY_AUDIT("SecurityAudit", "auditer la sécurité", "audit de sécurité", HttpStatus.OK, false);
    
    // ==================== PROPRIÉTÉS ====================
    
    private final String name;
    private final String verb;
    private final String noun;
    private final HttpStatus successStatus;
    private final boolean modifiesData;
    
    /**
     * Constructeur de l'enum Operation
     * 
     * @param name Nom de l'opération en anglais
     * @param verb Verbe d'action en français
     * @param noun Nom de l'action en français
     * @param successStatus Status HTTP en cas de succès
     * @param modifiesData true si l'opération modifie les données
     */
    Operation(String name, String verb, String noun, HttpStatus successStatus, boolean modifiesData) {
        this.name = name;
        this.verb = verb;
        this.noun = noun;
        this.successStatus = successStatus;
        this.modifiesData = modifiesData;
    }
    
    // ==================== GETTERS ====================
    
    /**
     * Obtient le nom de l'opération
     * @return Nom de l'opération
     */
    public String getName() {
        return name;
    }
    
    /**
     * Obtient le verbe d'action en français
     * @return Verbe d'action
     */
    public String getVerb() {
        return verb;
    }
    
    /**
     * Obtient le nom de l'action en français
     * @return Nom de l'action
     */
    public String getNoun() {
        return noun;
    }
    
    /**
     * Obtient le status HTTP de succès pour cette opération
     * @return Status HTTP
     */
    public HttpStatus getSuccessStatus() {
        return successStatus;
    }
    
    /**
     * Indique si l'opération modifie les données
     * @return true si l'opération modifie les données
     */
    public boolean modifiesData() {
        return modifiesData;
    }
    
    // ==================== MÉTHODES UTILITAIRES ====================
    
    /**
     * Trouve une Operation par nom
     * @param name Nom de l'opération à chercher
     * @return Operation correspondante ou null si non trouvée
     */
    public static Operation fromName(String name) {
        for (Operation op : values()) {
            if (op.name.equalsIgnoreCase(name)) {
                return op;
            }
        }
        return null;
    }
    
    /**
     * Vérifie si l'opération est une opération CRUD de base
     * @return true si c'est une opération CRUD
     */
    public boolean isCrudOperation() {
        return this == CREATE || this == READ || this == UPDATE || this == DELETE;
    }
    
    /**
     * Vérifie si l'opération est en lecture seule
     * @return true si l'opération ne modifie pas les données
     */
    public boolean isReadOnly() {
        return !modifiesData;
    }
    
    /**
     * Vérifie si l'opération nécessite une authentification
     * @return true si l'authentification est requise
     */
    public boolean requiresAuthentication() {
        return this != READ && this != LIST && this != SEARCH && this != COUNT && 
               this != EXISTS && this != CHECK_AVAILABILITY;
    }
    
    /**
     * Vérifie si l'opération nécessite des privilèges administrateur
     * @return true si des privilèges admin sont requis
     */
    public boolean requiresAdminPrivileges() {
        return this == BACKUP || this == RESTORE || this == SECURITY_AUDIT || 
               this == IMPORT || this == ACTIVATE_SALON || this == DEACTIVATE_SALON;
    }
    
    /**
     * Vérifie si l'opération est liée à l'authentification
     * @return true si c'est une opération d'authentification
     */
    public boolean isAuthOperation() {
        return this == LOGIN || this == LOGOUT || this == REGISTER || 
               this == CHANGE_PASSWORD || this == RESET_PASSWORD || this == VERIFY_EMAIL;
    }
    
    /**
     * Vérifie si l'opération est liée aux réservations
     * @return true si c'est une opération de réservation
     */
    public boolean isBookingOperation() {
        return this == BOOK || this == CONFIRM_BOOKING || this == CANCEL_BOOKING || 
               this == RESCHEDULE_BOOKING || this == CHECK_AVAILABILITY;
    }
    
    /**
     * Vérifie si l'opération est liée aux paiements
     * @return true si c'est une opération de paiement
     */
    public boolean isPaymentOperation() {
        return this == PROCESS_PAYMENT || this == CREATE_PAYMENT_SESSION || 
               this == REFUND || this == VALIDATE_PAYMENT;
    }
    
    /**
     * Vérifie si l'opération est liée aux notifications
     * @return true si c'est une opération de notification
     */
    public boolean isNotificationOperation() {
        return this == SEND_NOTIFICATION || this == SEND_EMAIL || this == SEND_SMS;
    }
    
    /**
     * Génère un message de succès pour cette opération
     * @param entityType Type d'entité concernée
     * @param identifier Identifiant de l'entité (optionnel)
     * @return Message de succès formaté
     */
    public String getSuccessMessage(EntityType entityType, Object identifier) {
        String message = entityType.getSuccessMessage(this.name, identifier);
        
        // Messages spécialisés pour certaines opérations
        return switch (this) {
            case LOGIN -> "Connexion réussie";
            case LOGOUT -> "Déconnexion réussie";
            case REGISTER -> "Inscription réussie";
            case BOOK -> "Réservation créée avec succès";
            case CONFIRM_BOOKING -> "Réservation confirmée";
            case CANCEL_BOOKING -> "Réservation annulée";
            case PROCESS_PAYMENT -> "Paiement traité avec succès";
            case REFUND -> "Remboursement effectué";
            default -> message;
        };
    }
    
    /**
     * Génère un message d'erreur pour cette opération
     * @param entityType Type d'entité concernée
     * @param identifier Identifiant de l'entité (optionnel)
     * @return Message d'erreur formaté
     */
    public String getErrorMessage(EntityType entityType, Object identifier) {
        String message = entityType.getErrorMessage(this.name, identifier);
        
        // Messages spécialisés pour certaines opérations
        return switch (this) {
            case LOGIN -> "Échec de la connexion";
            case REGISTER -> "Échec de l'inscription";
            case BOOK -> "Impossible de créer la réservation";
            case PROCESS_PAYMENT -> "Échec du traitement du paiement";
            case REFUND -> "Impossible d'effectuer le remboursement";
            default -> message;
        };
    }
    
    /**
     * Obtient le niveau de log approprié pour cette opération
     * @return Niveau de log (INFO, WARN, ERROR)
     */
    public String getLogLevel() {
        if (this.requiresAdminPrivileges() || this.isAuthOperation()) {
            return "WARN";
        } else if (this.modifiesData()) {
            return "INFO";
        } else {
            return "DEBUG";
        }
    }
    
    @Override
    public String toString() {
        return name;
    }
}