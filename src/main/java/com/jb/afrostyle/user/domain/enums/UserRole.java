package com.jb.afrostyle.user.domain.enums;

/**
 * Énumération définissant les différents rôles d'utilisateurs dans le système
 *
 * CUSTOMER: Client qui peut réserver des services
 * SALON_OWNER: Propriétaire de salon qui peut gérer son salon et ses services
 * ADMIN: Administrateur système avec tous les droits
 */
public enum UserRole {
    /**
     * Rôle client - peut consulter les salons et faire des réservations
     */
    CUSTOMER("Customer"),

    /**
     * Rôle propriétaire de salon - peut gérer son salon, ses services et voir ses réservations
     */
    SALON_OWNER("Salon Owner"),

    /**
     * Rôle administrateur - accès complet au système
     */
    ADMIN("Administrator");

    private final String displayName;

    /**
     * Constructeur de l'énumération
     * @param displayName Le nom d'affichage du rôle
     */
    UserRole(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Récupère le nom d'affichage du rôle
     * @return Le nom d'affichage
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Vérifie si le rôle a les privilèges administrateur
     * @return true si c'est un administrateur
     */
    public boolean isAdmin() {
        return this == ADMIN;
    }

    /**
     * Vérifie si le rôle peut gérer un salon
     * @return true si c'est un propriétaire de salon ou un admin
     */
    public boolean canManageSalon() {
        return this == SALON_OWNER || this == ADMIN;
    }
}