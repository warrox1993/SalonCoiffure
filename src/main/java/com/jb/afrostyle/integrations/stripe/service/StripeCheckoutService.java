package com.jb.afrostyle.integrations.stripe.service;

import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;

import java.math.BigDecimal;

/**
 * Service pour gérer les paiements via Stripe Checkout
 */
public interface StripeCheckoutService {

    /**
     * Crée une session Stripe Checkout
     * MONO-SALON : Plus besoin de salonId car il n'y a qu'un seul salon
     * 
     * @param amount Montant en EUR
     * @param bookingId ID de la réservation
     * @param customerId ID du client
     * @param description Description du paiement
     * @param successUrl URL de redirection après paiement réussi
     * @param cancelUrl URL de redirection après annulation
     * @return Session Stripe Checkout créée
     * @throws StripeException En cas d'erreur Stripe
     */
    Session createCheckoutSession(
            BigDecimal amount,
            Long bookingId,
            Long customerId,
            String description,
            String successUrl,
            String cancelUrl
    ) throws StripeException;

    /**
     * Récupère une session Stripe Checkout
     * 
     * @param sessionId ID de la session
     * @return Session Stripe Checkout
     * @throws StripeException En cas d'erreur Stripe
     */
    Session retrieveSession(String sessionId) throws StripeException;

    /**
     * Expire une session Stripe Checkout
     * 
     * @param sessionId ID de la session à expirer
     * @return Session expirée
     * @throws StripeException En cas d'erreur Stripe
     */
    Session expireSession(String sessionId) throws StripeException;

    /**
     * Traite un webhook Stripe Checkout
     * 
     * @param payload Payload du webhook
     * @param signature Signature Stripe
     * @throws StripeException En cas d'erreur Stripe
     */
    void processCheckoutWebhook(String payload, String signature) throws StripeException;

    /**
     * Traite un événement Stripe spécifique
     * 
     * @param event Événement Stripe
     */
    void handleStripeEvent(Event event);
}