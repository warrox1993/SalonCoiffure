package com.jb.afrostyle.notifications.domain.enums;

public enum NotificationChannel {
    EMAIL("Email"),
    SMS("SMS"),
    WEB_PUSH("Notification Web"),
    WEBSOCKET("WebSocket");

    private final String description;

    NotificationChannel(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isRealTime() {
        return this == WEBSOCKET || this == WEB_PUSH;
    }

    public boolean requiresScheduling() {
        return this == EMAIL || this == SMS;
    }
}