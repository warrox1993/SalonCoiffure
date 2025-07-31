package com.jb.afrostyle.notifications.domain.enums;

public enum NotificationStatus {
    PENDING("En attente d'envoi"),
    PROCESSING("En cours d'envoi"),
    SENT("Envoyée avec succès"),
    FAILED("Échec d'envoi"),
    CANCELLED("Annulée");

    private final String description;

    NotificationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isFinal() {
        return this == SENT || this == CANCELLED;
    }

    public boolean isError() {
        return this == FAILED;
    }

    public boolean canBeRetried() {
        return this == FAILED;
    }
}