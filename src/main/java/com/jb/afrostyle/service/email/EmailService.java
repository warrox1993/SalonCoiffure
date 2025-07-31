package com.jb.afrostyle.service.email;

import java.util.concurrent.CompletableFuture;

/**
 * Service centralisé pour l'envoi d'emails dans l'application AfroStyle.
 * 
 * Ce service unifie toute la logique d'envoi d'emails qui était précédemment
 * dispersée entre plusieurs services (UserService, BookingService).
 * 
 * Il fournit des méthodes pour :
 * - Envoi d'emails simples (texte)
 * - Envoi d'emails HTML avec templates
 * - Emails spécifiques aux utilisateurs (activation, reset password)
 * - Emails spécifiques aux réservations (confirmation, rappels)
 * 
 * Toutes les méthodes sont asynchrones pour éviter de bloquer les requêtes utilisateur
 * lors de l'envoi d'emails qui peuvent prendre du temps.
 * 
 * @author AfroStyle Team
 * @since 1.0
 */
public interface EmailService {

    // === MÉTHODES GÉNÉRIQUES ===
    
    /**
     * Envoie un email simple en texte brut de manière asynchrone.
     * 
     * Utilisé pour les notifications simples qui ne nécessitent pas
     * de formatage HTML complexe.
     * 
     * @param to Adresse email du destinataire
     * @param subject Sujet de l'email
     * @param textContent Contenu de l'email en texte brut
     * @return CompletableFuture qui se complète quand l'email est envoyé
     */
    CompletableFuture<Void> sendTextEmail(String to, String subject, String textContent);
    
    /**
     * Envoie un email avec contenu HTML de manière asynchrone.
     * 
     * Utilisé pour les emails avec un design professionnel (templates HTML,
     * logos, styles CSS intégrés).
     * 
     * @param to Adresse email du destinataire
     * @param subject Sujet de l'email
     * @param htmlContent Contenu HTML de l'email
     * @return CompletableFuture qui se complète quand l'email est envoyé
     */
    CompletableFuture<Void> sendHtmlEmail(String to, String subject, String htmlContent);

    // === EMAILS UTILISATEURS (ex-UserService) ===
    
    /**
     * Envoie un email de réinitialisation de mot de passe.
     * 
     * Contient un lien sécurisé temporaire permettant à l'utilisateur
     * de définir un nouveau mot de passe. Le lien expire après un délai
     * configuré dans l'application.
     * 
     * @param email Adresse email de l'utilisateur
     * @param resetLink Lien de réinitialisation sécurisé (avec token)
     * @return CompletableFuture qui se complète quand l'email est envoyé
     */
    CompletableFuture<Void> sendPasswordResetEmail(String email, String resetLink);
    
    /**
     * Envoie un email d'activation de compte.
     * 
     * Envoyé lors de l'inscription d'un nouvel utilisateur. Contient un lien
     * d'activation qui permet de vérifier l'adresse email et d'activer le compte.
     * 
     * @param email Adresse email de l'utilisateur
     * @param activationLink Lien d'activation sécurisé
     * @return CompletableFuture qui se complète quand l'email est envoyé
     */
    CompletableFuture<Void> sendActivationEmail(String email, String activationLink);
    
    /**
     * Envoie un email de bienvenue après l'inscription.
     * 
     * Email de bienvenue envoyé après la création réussie du compte.
     * Contient les informations importantes et les premiers pas sur la plateforme.
     * 
     * @param email Adresse email de l'utilisateur
     * @param username Nom d'utilisateur choisi
     * @return CompletableFuture qui se complète quand l'email est envoyé
     */
    CompletableFuture<Void> sendWelcomeEmail(String email, String username);

    // === EMAILS RÉSERVATIONS (ex-BookingService) ===
    
    /**
     * Envoie un email de confirmation de réservation.
     * 
     * Envoyé immédiatement après qu'une réservation soit confirmée.
     * Contient tous les détails de la réservation (date, heure, salon, services).
     * 
     * @param to Adresse email du client
     * @param customerName Nom du client
     * @param salonName Nom du salon
     * @param bookingDetails Détails formatés de la réservation
     * @return CompletableFuture qui se complète quand l'email est envoyé
     */
    CompletableFuture<Void> sendBookingConfirmationEmail(String to, String customerName, 
                                                         String salonName, String bookingDetails);
    
    /**
     * Envoie un email de rappel de réservation.
     * 
     * Envoyé automatiquement X heures avant le rendez-vous pour rappeler
     * au client sa réservation et éviter les no-shows.
     * 
     * @param to Adresse email du client
     * @param customerName Nom du client
     * @param salonName Nom du salon
     * @param reminderDetails Détails du rendez-vous à rappeler
     * @return CompletableFuture qui se complète quand l'email est envoyé
     */
    CompletableFuture<Void> sendBookingReminderEmail(String to, String customerName,
                                                     String salonName, String reminderDetails);
    
    /**
     * Envoie un email de notification de nouvelle disponibilité.
     * 
     * Envoyé aux clients intéressés quand un nouveau créneau devient disponible
     * dans leur salon préféré (système de liste d'attente/notifications).
     * 
     * @param to Adresse email du client
     * @param customerName Nom du client
     * @param salonName Nom du salon
     * @param availabilityDetails Détails du nouveau créneau disponible
     * @return CompletableFuture qui se complète quand l'email est envoyé
     */
    CompletableFuture<Void> sendNewAvailabilityEmail(String to, String customerName,
                                                     String salonName, String availabilityDetails);

    // === EMAILS PAIEMENTS ===
    
    /**
     * Envoie un email de confirmation de paiement.
     * 
     * Envoyé après un paiement réussi, contient les détails de la transaction
     * et sert de reçu électronique pour le client.
     * 
     * @param to Adresse email du client
     * @param customerName Nom du client
     * @param paymentAmount Montant payé
     * @param transactionId Identifiant de la transaction
     * @param serviceDetails Détails des services payés
     * @return CompletableFuture qui se complète quand l'email est envoyé
     */
    CompletableFuture<Void> sendPaymentConfirmationEmail(String to, String customerName,
                                                         String paymentAmount, String transactionId,
                                                         String serviceDetails);
}