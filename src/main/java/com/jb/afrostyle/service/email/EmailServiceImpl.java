package com.jb.afrostyle.service.email;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.concurrent.CompletableFuture;

/**
 * Implémentation centralisée du service d'envoi d'emails pour l'application AfroStyle.
 * 
 * Cette classe remplace et unifie les anciennes implémentations dispersées :
 * - com.jb.afrostyle.user.service.impl.EmailServiceImpl (vide, que des TODOs)
 * - com.jb.afrostyle.booking.service.BookingEmailService (implémentation complète)
 * 
 * ARCHITECTURE :
 * - Toutes les méthodes sont @Async pour éviter de bloquer les requêtes utilisateur
 * - Utilise JavaMailSender de Spring Boot pour l'envoi effectif
 * - Support des emails en texte brut ET HTML avec templates intégrés
 * - Logs détaillés pour chaque envoi (succès/échec)
 * - Gestion centralisée des erreurs d'envoi
 * 
 * CONFIGURATION REQUISE :
 * - spring.mail.* : Configuration SMTP dans application.properties
 * - email.from : Adresse expéditeur
 * - email.from-name : Nom affiché de l'expéditeur
 * 
 * @author AfroStyle Team
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    /**
     * Spring Boot JavaMailSender configuré automatiquement via les propriétés spring.mail.*
     * Injecté automatiquement si la configuration SMTP est présente.
     */
    private final JavaMailSender mailSender;

    /**
     * Adresse email utilisée comme expéditeur pour tous les emails.
     * Configurée via la propriété email.from dans application.properties
     */
    @Value("${email.from}")
    private String fromAddress;

    /**
     * Nom affiché de l'expéditeur dans les clients email.
     * Configurée via la propriété email.from-name dans application.properties
     */
    @Value("${email.from-name}")
    private String fromName;

    // === IMPLÉMENTATION MÉTHODES GÉNÉRIQUES ===

    @Override
    @Async
    public CompletableFuture<Void> sendTextEmail(String to, String subject, String textContent) {
        log.info("📧 Sending text email to: {} | Subject: {}", to, subject);
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(textContent);

            mailSender.send(message);
            
            log.info("✅ Text email sent successfully to: {}", to);
            return CompletableFuture.completedFuture(null);
            
        } catch (Exception e) {
            log.error("❌ Failed to send text email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Text email sending failed", e);
        }
    }

    @Override
    @Async
    public CompletableFuture<Void> sendHtmlEmail(String to, String subject, String htmlContent) {
        log.info("📧 Sending HTML email to: {} | Subject: {}", to, subject);
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Configuration de l'expéditeur avec gestion d'erreur pour le nom d'affichage
            try {
                helper.setFrom(fromAddress, fromName);
            } catch (java.io.UnsupportedEncodingException e) {
                log.warn("⚠️ Could not set sender name, using address only: {}", e.getMessage());
                helper.setFrom(fromAddress);
            }
            
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = HTML content
            
            mailSender.send(message);
            
            log.info("✅ HTML email sent successfully to: {}", to);
            return CompletableFuture.completedFuture(null);
            
        } catch (MessagingException e) {
            log.error("❌ Failed to send HTML email to {}: {}", to, e.getMessage());
            throw new RuntimeException("HTML email sending failed", e);
        }
    }

    // === IMPLÉMENTATION EMAILS UTILISATEURS ===

    @Override
    @Async
    public CompletableFuture<Void> sendPasswordResetEmail(String email, String resetLink) {
        log.info("🔐 Sending password reset email to: {}", email);
        
        String subject = "Réinitialisation de votre mot de passe - AfroStyle";
        String htmlContent = createPasswordResetHtml(resetLink);
        
        return sendHtmlEmail(email, subject, htmlContent);
    }

    @Override
    @Async
    public CompletableFuture<Void> sendActivationEmail(String email, String activationLink) {
        log.info("🎉 Sending account activation email to: {}", email);
        
        String subject = "Activez votre compte AfroStyle";
        String htmlContent = createActivationHtml(activationLink);
        
        return sendHtmlEmail(email, subject, htmlContent);
    }

    @Override
    @Async
    public CompletableFuture<Void> sendWelcomeEmail(String email, String username) {
        log.info("👋 Sending welcome email to: {} (user: {})", email, username);
        
        String subject = "Bienvenue sur AfroStyle !";
        String htmlContent = createWelcomeHtml(username);
        
        return sendHtmlEmail(email, subject, htmlContent);
    }

    // === IMPLÉMENTATION EMAILS RÉSERVATIONS ===

    @Override
    @Async
    public CompletableFuture<Void> sendBookingConfirmationEmail(String to, String customerName, 
                                                              String salonName, String bookingDetails) {
        log.info("📅 Sending booking confirmation email to: {} for salon: {}", to, salonName);
        
        String subject = "Confirmation de réservation - " + salonName;
        String htmlContent = createBookingConfirmationHtml(customerName, salonName, bookingDetails);
        
        return sendHtmlEmail(to, subject, htmlContent);
    }

    @Override
    @Async
    public CompletableFuture<Void> sendBookingReminderEmail(String to, String customerName,
                                                           String salonName, String reminderDetails) {
        log.info("⏰ Sending booking reminder email to: {} for salon: {}", to, salonName);
        
        String subject = "Rappel de rendez-vous - " + salonName;
        String htmlContent = createBookingReminderHtml(customerName, salonName, reminderDetails);
        
        return sendHtmlEmail(to, subject, htmlContent);
    }

    @Override
    @Async
    public CompletableFuture<Void> sendNewAvailabilityEmail(String to, String customerName,
                                                           String salonName, String availabilityDetails) {
        log.info("🎯 Sending new availability email to: {} for salon: {}", to, salonName);
        
        String subject = "Nouveau créneau disponible - " + salonName;
        String htmlContent = createNewAvailabilityHtml(customerName, salonName, availabilityDetails);
        
        return sendHtmlEmail(to, subject, htmlContent);
    }

    // === IMPLÉMENTATION EMAILS PAIEMENTS ===

    @Override
    @Async
    public CompletableFuture<Void> sendPaymentConfirmationEmail(String to, String customerName,
                                                               String paymentAmount, String transactionId,
                                                               String serviceDetails) {
        log.info("💳 Sending payment confirmation email to: {} | Amount: {} | Transaction: {}", 
                 to, paymentAmount, transactionId);
        
        String subject = "Confirmation de paiement - AfroStyle";
        String htmlContent = createPaymentConfirmationHtml(customerName, paymentAmount, 
                                                           transactionId, serviceDetails);
        
        return sendHtmlEmail(to, subject, htmlContent);
    }

    // === MÉTHODES PRIVÉES POUR TEMPLATES HTML ===

    /**
     * Crée le template HTML pour l'email de réinitialisation de mot de passe.
     * Design professionnel avec couleurs AfroStyle et call-to-action clair.
     */
    private String createPasswordResetHtml(String resetLink) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #e74c3c; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { padding: 30px; background: #f9f9f9; }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                    .button { display: inline-block; background: #e74c3c; color: white; padding: 15px 30px; 
                             text-decoration: none; border-radius: 5px; font-weight: bold; margin: 20px 0; }
                    .warning { background: #ffebee; border-left: 4px solid #e74c3c; padding: 15px; margin: 20px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🔐 Réinitialisation de mot de passe</h1>
                    </div>
                    <div class="content">
                        <p>Bonjour,</p>
                        
                        <p>Vous avez demandé la réinitialisation de votre mot de passe AfroStyle.</p>
                        
                        <div class="warning">
                            <strong>⚠️ Important :</strong> Ce lien expire dans 24 heures pour votre sécurité.
                        </div>
                        
                        <p style="text-align: center;">
                            <a href="%s" class="button">Réinitialiser mon mot de passe</a>
                        </p>
                        
                        <p><small>Si vous n'avez pas demandé cette réinitialisation, ignorez cet email. Votre compte reste sécurisé.</small></p>
                    </div>
                    <div class="footer">
                        <p>© 2025 AfroStyle - Votre beauté, notre passion</p>
                    </div>
                </div>
            </body>
            </html>
            """, resetLink);
    }

    /**
     * Crée le template HTML pour l'email d'activation de compte.
     * Design accueillant pour les nouveaux utilisateurs.
     */
    private String createActivationHtml(String activationLink) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #27ae60; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { padding: 30px; background: #f9f9f9; }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                    .button { display: inline-block; background: #27ae60; color: white; padding: 15px 30px; 
                             text-decoration: none; border-radius: 5px; font-weight: bold; margin: 20px 0; }
                    .highlight { background: #e8f5e8; padding: 15px; border-radius: 5px; margin: 15px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎉 Activez votre compte</h1>
                    </div>
                    <div class="content">
                        <p>Félicitations !</p>
                        
                        <p>Votre compte AfroStyle a été créé avec succès. Il ne reste plus qu'à activer votre adresse email.</p>
                        
                        <div class="highlight">
                            <p><strong>Pourquoi activer ?</strong><br>
                            L'activation confirme que cette adresse email vous appartient et sécurise votre compte.</p>
                        </div>
                        
                        <p style="text-align: center;">
                            <a href="%s" class="button">Activer mon compte</a>
                        </p>
                        
                        <p><small>Ce lien d'activation expire dans 48 heures.</small></p>
                    </div>
                    <div class="footer">
                        <p>© 2025 AfroStyle - Votre beauté, notre passion</p>
                    </div>
                </div>
            </body>
            </html>
            """, activationLink);
    }

    /**
     * Crée le template HTML pour l'email de bienvenue.
     * Premier contact chaleureux avec le nouvel utilisateur.
     */
    private String createWelcomeHtml(String username) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #F9B233; color: #673802; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { padding: 30px; background: #f9f9f9; }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                    .features { background: #FFE8BC; padding: 20px; border-radius: 5px; margin: 20px 0; }
                    .feature-list { list-style: none; padding: 0; }
                    .feature-list li { margin: 10px 0; }
                    .feature-list li:before { content: "✨ "; color: #F9B233; font-weight: bold; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>👋 Bienvenue %s !</h1>
                    </div>
                    <div class="content">
                        <p>Nous sommes ravis de vous accueillir dans la famille AfroStyle !</p>
                        
                        <div class="features">
                            <h3>Découvrez ce que vous pouvez faire :</h3>
                            <ul class="feature-list">
                                <li>Réserver facilement dans vos salons préférés</li>
                                <li>Recevoir des rappels automatiques de rendez-vous</li>
                                <li>Payer en ligne en toute sécurité</li>
                                <li>Être notifié des nouvelles disponibilités</li>
                                <li>Gérer votre historique de réservations</li>
                            </ul>
                        </div>
                        
                        <p>Votre aventure beauté commence maintenant. Prêt(e) à prendre votre premier rendez-vous ?</p>
                        
                        <p><strong>L'équipe AfroStyle</strong><br>
                        <em>Votre beauté, notre passion</em></p>
                    </div>
                    <div class="footer">
                        <p>© 2025 AfroStyle - Votre beauté, notre passion</p>
                    </div>
                </div>
            </body>
            </html>
            """, username);
    }

    /**
     * Crée le template HTML pour la confirmation de réservation.
     * Repris du BookingEmailService existant et amélioré.
     */
    private String createBookingConfirmationHtml(String customerName, String salonName, String details) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #8c5a12; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { padding: 30px; background: #f9f9f9; }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                    .highlight { background: #FFE8BC; padding: 20px; border-radius: 5px; margin: 20px 0; border-left: 4px solid #F9B233; }
                    .button { display: inline-block; background: #F9B233; color: #673802; padding: 12px 24px; 
                             text-decoration: none; border-radius: 5px; font-weight: bold; margin: 15px 0; }
                    .success-icon { color: #27ae60; font-size: 24px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1><span class="success-icon">✅</span> Réservation Confirmée !</h1>
                    </div>
                    <div class="content">
                        <p>Bonjour <strong>%s</strong>,</p>
                        
                        <p>Excellente nouvelle ! Votre réservation chez <strong>%s</strong> a été confirmée avec succès.</p>
                        
                        <div class="highlight">
                            <h3>📋 Détails de votre réservation :</h3>
                            %s
                        </div>
                        
                        <p>Nous avons hâte de vous accueillir ! En cas de besoin, n'hésitez pas à nous contacter.</p>
                        
                        <p style="text-align: center;">
                            <a href="#" class="button">Voir ma réservation</a>
                        </p>
                        
                        <p><small><strong>💡 Conseil :</strong> Arrivez 10 minutes avant votre rendez-vous pour profiter pleinement de votre expérience.</small></p>
                    </div>
                    <div class="footer">
                        <p>© 2025 AfroStyle - Votre beauté, notre passion</p>
                        <p>Pour modifier ou annuler, contactez directement le salon</p>
                    </div>
                </div>
            </body>
            </html>
            """, customerName, salonName, details);
    }

    /**
     * Crée le template HTML pour les rappels de réservation.
     */
    private String createBookingReminderHtml(String customerName, String salonName, String details) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #e74c3c; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { padding: 30px; background: #f9f9f9; }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                    .highlight { background: #FFE8BC; padding: 20px; border-radius: 5px; margin: 20px 0; }
                    .urgent { background: #ffebee; border-left: 4px solid #e74c3c; padding: 15px; margin: 15px 0; }
                    .reminder-icon { color: #e74c3c; font-size: 24px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1><span class="reminder-icon">⏰</span> Rappel de Rendez-vous</h1>
                    </div>
                    <div class="content">
                        <p>Bonjour <strong>%s</strong>,</p>
                        
                        <div class="urgent">
                            <p><strong>🔔 N'oubliez pas votre rendez-vous chez %s !</strong></p>
                        </div>
                        
                        <div class="highlight">
                            <h3>📋 Détails de votre rendez-vous :</h3>
                            %s
                        </div>
                        
                        <p><strong>⚠️ Important :</strong> Merci de nous prévenir au plus vite en cas d'empêchement.</p>
                        
                        <p>À très bientôt ! 😊</p>
                    </div>
                    <div class="footer">
                        <p>© 2025 AfroStyle - Votre beauté, notre passion</p>
                    </div>
                </div>
            </body>
            </html>
            """, customerName, salonName, details);
    }

    /**
     * Crée le template HTML pour les notifications de nouvelles disponibilités.
     */
    private String createNewAvailabilityHtml(String customerName, String salonName, String details) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #27ae60; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { padding: 30px; background: #f9f9f9; }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                    .highlight { background: #e8f5e8; padding: 20px; border-radius: 5px; margin: 20px 0; border-left: 4px solid #27ae60; }
                    .button { display: inline-block; background: #27ae60; color: white; padding: 15px 30px; 
                             text-decoration: none; border-radius: 5px; font-weight: bold; margin: 15px 0; }
                    .opportunity-icon { color: #27ae60; font-size: 24px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1><span class="opportunity-icon">🎉</span> Nouveau Créneau Disponible !</h1>
                    </div>
                    <div class="content">
                        <p>Bonjour <strong>%s</strong>,</p>
                        
                        <p>Excellente nouvelle ! Un nouveau créneau vient de se libérer chez <strong>%s</strong> :</p>
                        
                        <div class="highlight">
                            <h3>📅 Créneau disponible :</h3>
                            %s
                        </div>
                        
                        <p><strong>⚡ Dépêchez-vous !</strong> Les créneaux de qualité partent très vite.</p>
                        
                        <p style="text-align: center;">
                            <a href="#" class="button">Réserver Maintenant</a>
                        </p>
                        
                        <p><small>Vous recevez cet email car vous avez activé les notifications de disponibilité pour ce salon.</small></p>
                    </div>
                    <div class="footer">
                        <p>© 2025 AfroStyle - Votre beauté, notre passion</p>
                        <p>Pour vous désabonner des notifications, <a href="#">cliquez ici</a></p>
                    </div>
                </div>
            </body>
            </html>
            """, customerName, salonName, details);
    }

    /**
     * Crée le template HTML pour la confirmation de paiement.
     * Nouveau template pour les reçus de paiement électroniques.
     */
    private String createPaymentConfirmationHtml(String customerName, String paymentAmount, 
                                                String transactionId, String serviceDetails) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #8c5a12; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { padding: 30px; background: #f9f9f9; }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                    .receipt { background: white; padding: 20px; border: 2px solid #F9B233; border-radius: 5px; margin: 20px 0; }
                    .amount { font-size: 24px; color: #27ae60; font-weight: bold; text-align: center; margin: 15px 0; }
                    .transaction { background: #f8f9fa; padding: 15px; border-radius: 3px; margin: 15px 0; }
                    .success-icon { color: #27ae60; font-size: 24px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1><span class="success-icon">💳</span> Paiement Confirmé</h1>
                    </div>
                    <div class="content">
                        <p>Bonjour <strong>%s</strong>,</p>
                        
                        <p>Votre paiement a été traité avec succès ! Voici votre reçu électronique :</p>
                        
                        <div class="receipt">
                            <h3>🧾 Reçu de paiement</h3>
                            
                            <div class="amount">Montant payé : %s €</div>
                            
                            <div class="transaction">
                                <strong>🔍 Référence transaction :</strong><br>
                                <code>%s</code>
                            </div>
                            
                            <h4>📋 Détails des services :</h4>
                            %s
                            
                            <p><small>Conservez ce reçu pour vos records. Il vous sera demandé en cas de problème.</small></p>
                        </div>
                        
                        <p>Merci de votre confiance ! Votre réservation est maintenant entièrement confirmée.</p>
                    </div>
                    <div class="footer">
                        <p>© 2025 AfroStyle - Votre beauté, notre passion</p>
                        <p>Questions sur votre paiement ? Contactez notre support client</p>
                    </div>
                </div>
            </body>
            </html>
            """, customerName, paymentAmount, transactionId, serviceDetails);
    }
}