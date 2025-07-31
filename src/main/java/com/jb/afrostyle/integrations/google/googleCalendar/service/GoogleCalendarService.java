package com.jb.afrostyle.integrations.google.googleCalendar.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.jb.afrostyle.booking.domain.entity.Booking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleCalendarService {

    private static final Logger log = LoggerFactory.getLogger(GoogleCalendarService.class);

    private static final String APPLICATION_NAME = "AfroStyle Salon Booking";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);
    private static final String CREDENTIALS_FILE_PATH = "/google-credentials.json";

    @Value("${google.calendar.enabled}")
    private boolean calendarEnabled;

    @Value("${google.calendar.timezone}")
    private String timeZone;

    private NetHttpTransport httpTransport;
    private Calendar calendar;

    public GoogleCalendarService() {
        try {
            this.httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            initializeCalendar();
        } catch (GeneralSecurityException | IOException e) {
            log.error("Erreur lors de l'initialisation du service Google Calendar", e);
        }
    }

    private void initializeCalendar() throws IOException {
        if (!calendarEnabled) {
            log.info("Google Calendar est désactivé");
            return;
        }

        try {
            Credential credential = getCredentials(httpTransport);
            calendar = new Calendar.Builder(httpTransport, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
            log.info("Service Google Calendar initialisé avec succès");
        } catch (Exception e) {
            log.warn("Impossible d'initialiser Google Calendar, fonctionnalité désactivée", e);
            calendarEnabled = false;
        }
    }

    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        InputStream in = GoogleCalendarService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Fichier de credentials non trouvé : " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();

        return flow.loadCredential("user");
    }

    public String createCalendarEvent(Booking booking) {
        if (!calendarEnabled || calendar == null) {
            log.debug("Google Calendar non disponible, événement non créé");
            return null;
        }

        try {
            Event event = buildEventFromBooking(booking);
            Event createdEvent = calendar.events().insert("primary", event).execute();
            
            log.info("Événement Google Calendar créé avec succès. ID: {}", createdEvent.getId());
            return createdEvent.getId();
            
        } catch (Exception e) {
            log.error("Erreur lors de la création de l'événement Google Calendar pour la réservation {}", 
                    booking.getId(), e);
            return null;
        }
    }

    public boolean updateCalendarEvent(String eventId, Booking booking) {
        if (!calendarEnabled || calendar == null || eventId == null) {
            return false;
        }

        try {
            Event event = buildEventFromBooking(booking);
            calendar.events().update("primary", eventId, event).execute();
            
            log.info("Événement Google Calendar mis à jour avec succès. ID: {}", eventId);
            return true;
            
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour de l'événement Google Calendar {}", eventId, e);
            return false;
        }
    }

    public boolean deleteCalendarEvent(String eventId) {
        if (!calendarEnabled || calendar == null || eventId == null) {
            return false;
        }

        try {
            calendar.events().delete("primary", eventId).execute();
            log.info("Événement Google Calendar supprimé avec succès. ID: {}", eventId);
            return true;
            
        } catch (Exception e) {
            log.error("Erreur lors de la suppression de l'événement Google Calendar {}", eventId, e);
            return false;
        }
    }

    private Event buildEventFromBooking(Booking booking) {
        Event event = new Event();
        
        // Titre de l'événement
        String summary = String.format("Rendez-vous - %s", 
                booking.getServiceName() != null ? booking.getServiceName() : "Service salon");
        event.setSummary(summary);
        
        // Description
        StringBuilder description = new StringBuilder();
        description.append("Réservation AfroStyle\n");
        description.append("Client: ").append(booking.getUserName() != null ? booking.getUserName() : "Non spécifié").append("\n");
        description.append("Service: ").append(booking.getServiceName() != null ? booking.getServiceName() : "Non spécifié").append("\n");
        description.append("Salon: ").append(booking.getSalonName() != null ? booking.getSalonName() : "Non spécifié").append("\n");
        description.append("Statut: ").append(booking.getStatus()).append("\n");
        if (booking.getNotes() != null && !booking.getNotes().isEmpty()) {
            description.append("Notes: ").append(booking.getNotes()).append("\n");
        }
        description.append("ID Réservation: ").append(booking.getId());
        event.setDescription(description.toString());
        
        // Dates de début et fin
        LocalDateTime startTime = booking.getBookingDate();
        LocalDateTime endTime = startTime.plusHours(1); // Durée par défaut de 1 heure
        
        DateTime start = new DateTime(
                startTime.atZone(ZoneId.of(timeZone)).toInstant().toEpochMilli()
        );
        event.setStart(new EventDateTime().setDateTime(start).setTimeZone(timeZone));
        
        DateTime end = new DateTime(
                endTime.atZone(ZoneId.of(timeZone)).toInstant().toEpochMilli()
        );
        event.setEnd(new EventDateTime().setDateTime(end).setTimeZone(timeZone));
        
        // Localisation (si disponible)
        if (booking.getSalonName() != null) {
            event.setLocation(booking.getSalonName());
        }
        
        return event;
    }

    public boolean isCalendarEnabled() {
        return calendarEnabled && calendar != null;
    }

    public List<Event> getUpcomingEvents(int maxResults) {
        if (!calendarEnabled || calendar == null) {
            return Collections.emptyList();
        }

        try {
            DateTime now = new DateTime(System.currentTimeMillis());
            return calendar.events().list("primary")
                    .setMaxResults(maxResults)
                    .setTimeMin(now)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute()
                    .getItems();
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des événements Google Calendar", e);
            return Collections.emptyList();
        }
    }
}