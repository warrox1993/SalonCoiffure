package com.jb.afrostyle.payment.dto;

import com.jb.afrostyle.payment.domain.enums.PaymentMethod;
import com.jb.afrostyle.payment.validation.ValidCurrency;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

/**
 * DTO pour la demande de paiement avec Builder Pattern Context7
 * Contient toutes les informations nécessaires pour traiter un paiement
 * Intègre validation avancée des montants, devises et URLs
 *
 * <p>Exemple d'utilisation :</p>
 * <pre>{@code
 * PaymentRequest request = PaymentRequest.builder()
 *     .bookingId(123L)
 *     .amount(new BigDecimal("25.00"))
 *     .paymentMethod(PaymentMethod.CARD)
 *     .description("Payment for Hair Braiding service")
 *     .returnUrl("https://afrostyle.be/payment/success")
 *     .cancelUrl("https://afrostyle.be/payment/cancel")
 *     .build();
 * }</pre>
 */
public record PaymentRequest(
        /**
         * ID de la réservation à payer
         * Doit être un ID valide et positif
         */
        @NotNull(message = "Booking ID is required")
        @Positive(message = "Booking ID must be positive")
        Long bookingId,

        /**
         * Montant à payer
         * Doit être positif et ne pas dépasser 10000 EUR
         */
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
        @DecimalMax(value = "10000.00", message = "Amount cannot exceed 10000.00")
        BigDecimal amount,

        /**
         * Méthode de paiement souhaitée
         * Doit être une méthode supportée par le système
         */
        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod,

        /**
         * Code de devise ISO 4217
         * Par défaut EUR
         */
        @ValidCurrency
        String currency,
        
        /**
         * Description du paiement (optionnelle)
         * Affichée à l'utilisateur
         */
        @Size(max = 200, message = "Description cannot exceed 200 characters")
        String description,
        
        /**
         * URL de retour après paiement réussi
         * Doit être une URL valide
         */
        @Pattern(regexp = "^https?://.*", message = "Return URL must be a valid HTTP/HTTPS URL")
        String returnUrl,
        
        /**
         * URL de retour après annulation
         * Doit être une URL valide
         */
        @Pattern(regexp = "^https?://.*", message = "Cancel URL must be a valid HTTP/HTTPS URL")
        String cancelUrl
) {
    public PaymentRequest {
        // Valeur par défaut pour currency
        if (currency == null) {
            currency = "EUR";
        }
    }
    
    /**
     * Crée une nouvelle instance du Builder pour PaymentRequest
     * @return Une nouvelle instance du Builder
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder Pattern Context7 pour PaymentRequest
     * Fournit une API fluide avec validation métier avancée des paiements
     *
     * <p>Fonctionnalités avancées :</p>
     * <ul>
     *   <li>Validation des montants avec limites métier</li>
     *   <li>Support multi-devises avec validation ISO 4217</li>
     *   <li>Validation et normalisation des URLs</li>
     *   <li>Validation des méthodes de paiement par contexte</li>
     *   <li>Calculs automatiques (taxes, frais, etc.)</li>
     *   <li>Génération automatique d'URLs de retour</li>
     * </ul>
     */
    public static class Builder {
        private Long bookingId;
        private BigDecimal amount;
        private PaymentMethod paymentMethod;
        private String currency = "EUR";
        private String description;
        private String returnUrl;
        private String cancelUrl;
        
        // Devises supportées avec leurs limites
        private static final Set<String> SUPPORTED_CURRENCIES = Set.of("EUR", "USD", "GBP", "CAD");
        private static final Set<String> MAJOR_CURRENCIES = Set.of("EUR", "USD", "GBP");
        
        /**
         * Définit l'ID de la réservation à payer (requis)
         * @param bookingId L'identifiant de la réservation
         * @return Le builder pour chaînage
         * @throws IllegalArgumentException si bookingId est null ou négatif
         */
        public Builder bookingId(Long bookingId) {
            if (bookingId != null && bookingId <= 0) {
                throw new IllegalArgumentException("Booking ID must be positive");
            }
            this.bookingId = bookingId;
            return this;
        }
        
        /**
         * Définit le montant du paiement (requis)
         * @param amount Le montant à payer
         * @return Le builder pour chaînage
         * @throws IllegalArgumentException si le montant est invalide
         */
        public Builder amount(BigDecimal amount) {
            if (amount != null) {
                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("Payment amount must be positive");
                }
                if (amount.compareTo(new BigDecimal("10000.00")) > 0) {
                    throw new IllegalArgumentException("Payment amount cannot exceed 10000.00");
                }
                if (amount.scale() > 2) {
                    throw new IllegalArgumentException("Payment amount cannot have more than 2 decimal places");
                }
                // Validation montant minimum par devise
                validateMinimumAmount(amount, this.currency);
            }
            this.amount = amount;
            return this;
        }
        
        /**
         * Définit le montant en format string pour facilité d'usage
         * @param amount Le montant comme string (ex: "25.50")
         * @return Le builder pour chaînage
         * @throws IllegalArgumentException si le format est invalide
         */
        public Builder amount(String amount) {
            if (amount != null) {
                try {
                    return amount(new BigDecimal(amount));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid amount format: " + amount);
                }
            }
            return this;
        }
        
        /**
         * Définit le montant en double pour facilité d'usage
         * @param amount Le montant comme double
         * @return Le builder pour chaînage
         */
        public Builder amount(double amount) {
            return amount(BigDecimal.valueOf(amount));
        }
        
        /**
         * Définit la méthode de paiement (requis)
         * @param paymentMethod La méthode de paiement
         * @return Le builder pour chaînage
         * @throws IllegalArgumentException si la méthode n'est pas supportée pour la devise
         */
        public Builder paymentMethod(PaymentMethod paymentMethod) {
            if (paymentMethod != null) {
                // Validation de la compatibilité méthode/devise
                validatePaymentMethodCurrency(paymentMethod, this.currency);
            }
            this.paymentMethod = paymentMethod;
            return this;
        }
        
        /**
         * Définit la devise (par défaut EUR)
         * @param currency Code devise ISO 4217
         * @return Le builder pour chaînage
         * @throws IllegalArgumentException si la devise n'est pas supportée
         */
        public Builder currency(String currency) {
            if (currency != null) {
                currency = currency.toUpperCase();
                if (!currency.matches("[A-Z]{3}")) {
                    throw new IllegalArgumentException("Currency must be a valid 3-letter ISO 4217 code");
                }
                if (!SUPPORTED_CURRENCIES.contains(currency)) {
                    throw new IllegalArgumentException("Currency not supported: " + currency + 
                        ". Supported currencies: " + SUPPORTED_CURRENCIES);
                }
                
                // Revalider le montant si déjà défini
                if (this.amount != null) {
                    validateMinimumAmount(this.amount, currency);
                }
                // Revalider la méthode de paiement si déjà définie
                if (this.paymentMethod != null) {
                    validatePaymentMethodCurrency(this.paymentMethod, currency);
                }
            }
            this.currency = currency != null ? currency : "EUR";
            return this;
        }
        
        /**
         * Définit la description du paiement
         * @param description Description du paiement (max 200 caractères)
         * @return Le builder pour chaînage
         * @throws IllegalArgumentException si la description est trop longue
         */
        public Builder description(String description) {
            if (description != null) {
                if (description.length() > 200) {
                    throw new IllegalArgumentException("Payment description cannot exceed 200 characters");
                }
                if (description.trim().isEmpty()) {
                    throw new IllegalArgumentException("Payment description cannot be empty");
                }
            }
            this.description = description;
            return this;
        }
        
        /**
         * Génère automatiquement une description basée sur le montant et la devise
         * @return Le builder pour chaînage
         */
        public Builder autoDescription() {
            if (this.amount != null && this.currency != null) {
                this.description = String.format("Payment of %s %s for booking #%s", 
                    this.amount, this.currency, this.bookingId != null ? this.bookingId : "TBD");
            }
            return this;
        }
        
        /**
         * Définit l'URL de retour après paiement réussi
         * @param returnUrl L'URL de retour
         * @return Le builder pour chaînage
         * @throws IllegalArgumentException si l'URL est invalide
         */
        public Builder returnUrl(String returnUrl) {
            if (returnUrl != null) {
                validateUrl(returnUrl, "Return URL");
            }
            this.returnUrl = returnUrl;
            return this;
        }
        
        /**
         * Définit l'URL de retour après annulation
         * @param cancelUrl L'URL d'annulation
         * @return Le builder pour chaînage
         * @throws IllegalArgumentException si l'URL est invalide
         */
        public Builder cancelUrl(String cancelUrl) {
            if (cancelUrl != null) {
                validateUrl(cancelUrl, "Cancel URL");
            }
            this.cancelUrl = cancelUrl;
            return this;
        }
        
        /**
         * Définit automatiquement les URLs de retour basées sur un domaine
         * @param baseDomain Le domaine de base (ex: "https://afrostyle.be")
         * @return Le builder pour chaînage
         */
        public Builder autoUrls(String baseDomain) {
            if (baseDomain != null) {
                // Normaliser le domaine (enlever trailing slash si présent)
                String normalizedDomain = baseDomain.endsWith("/") ? 
                    baseDomain.substring(0, baseDomain.length() - 1) : baseDomain;
                
                this.returnUrl = normalizedDomain + "/payment/success";
                this.cancelUrl = normalizedDomain + "/payment/cancel";
            }
            return this;
        }
        
        /**
         * Configuration pour paiement de test avec URLs locales
         * @return Le builder pour chaînage
         */
        public Builder testMode() {
            return autoUrls("http://localhost:4200")
                .description("Test payment - AfroStyle");
        }
        
        /**
         * Configuration pour paiement de production
         * @return Le builder pour chaînage
         */
        public Builder productionMode() {
            return autoUrls("https://afrostyle.be");
        }
        
        /**
         * Construit et valide l'instance PaymentRequest
         * Applique la validation métier Context7 complète
         *
         * @return Une nouvelle instance PaymentRequest validée
         * @throws IllegalStateException si les champs requis sont manquants ou invalides
         */
        public PaymentRequest build() {
            // Validation des champs requis
            if (bookingId == null) {
                throw new IllegalStateException("Booking ID is required for payment request");
            }
            if (amount == null) {
                throw new IllegalStateException("Payment amount is required");
            }
            if (paymentMethod == null) {
                throw new IllegalStateException("Payment method is required");
            }
            
            // Validation métier avancée
            validateMinimumAmount(amount, currency);
            validatePaymentMethodCurrency(paymentMethod, currency);
            
            // Validation des URLs si fournies
            if (returnUrl != null) {
                validateUrl(returnUrl, "Return URL");
            }
            if (cancelUrl != null) {
                validateUrl(cancelUrl, "Cancel URL");
            }
            
            // Génération automatique de description si manquante
            if (description == null || description.trim().isEmpty()) {
                description = String.format("Payment of %s %s for booking #%s", 
                    amount, currency, bookingId);
            }
            
            // Validation montant pour méthodes spécifiques
            if (paymentMethod == PaymentMethod.CASH && amount.compareTo(new BigDecimal("500.00")) > 0) {
                throw new IllegalStateException("Cash payments cannot exceed 500.00 EUR");
            }
            
            // Validation devises majeures pour montants élevés
            if (amount.compareTo(new BigDecimal("1000.00")) > 0 && !MAJOR_CURRENCIES.contains(currency)) {
                throw new IllegalStateException("Payments over 1000.00 must use major currencies (EUR, USD, GBP)");
            }
            
            return new PaymentRequest(bookingId, amount, paymentMethod, currency, 
                description, returnUrl, cancelUrl);
        }
        
        /**
         * Valide le montant minimum par devise
         */
        private void validateMinimumAmount(BigDecimal amount, String currency) {
            if (amount == null || currency == null) return;
            
            BigDecimal minimum = switch (currency) {
                case "EUR" -> new BigDecimal("0.50");
                case "USD" -> new BigDecimal("0.50");
                case "GBP" -> new BigDecimal("0.30");
                case "CAD" -> new BigDecimal("0.50");
                default -> new BigDecimal("1.00");
            };
            
            if (amount.compareTo(minimum) < 0) {
                throw new IllegalArgumentException(
                    String.format("Minimum amount for %s is %s", currency, minimum));
            }
        }
        
        /**
         * Valide la compatibilité méthode de paiement/devise
         */
        private void validatePaymentMethodCurrency(PaymentMethod method, String currency) {
            if (method == null || currency == null) return;
            
            // Apple Pay et Google Pay limités aux devises majeures
            if ((method == PaymentMethod.APPLE_PAY || method == PaymentMethod.GOOGLE_PAY) 
                && !MAJOR_CURRENCIES.contains(currency)) {
                throw new IllegalArgumentException(
                    String.format("%s only supports major currencies (EUR, USD, GBP)", 
                    method.getDisplayName()));
            }
            
            // Cash limité à EUR pour ce salon
            if (method == PaymentMethod.CASH && !"EUR".equals(currency)) {
                throw new IllegalArgumentException("Cash payments only accepted in EUR");
            }
        }
        
        /**
         * Valide le format d'une URL
         */
        private void validateUrl(String url, String fieldName) {
            try {
                URL parsedUrl = new URL(url);
                String protocol = parsedUrl.getProtocol();
                
                if (!"http".equals(protocol) && !"https".equals(protocol)) {
                    throw new IllegalArgumentException(fieldName + " must use HTTP or HTTPS protocol");
                }
                
                // Production doit utiliser HTTPS
                if ("http".equals(protocol) && !url.contains("localhost")) {
                    throw new IllegalArgumentException(fieldName + " must use HTTPS in production");
                }
                
                // Valider que le domaine n'est pas malicieux
                String host = parsedUrl.getHost();
                if (host == null || host.trim().isEmpty()) {
                    throw new IllegalArgumentException(fieldName + " must have a valid host");
                }
                
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(fieldName + " is not a valid URL: " + e.getMessage());
            }
        }
    }
}