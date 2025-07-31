package com.jb.afrostyle.core.config.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

/**
 * Configuration des paiements
 * Externalise les constantes liées aux payments
 * 
 * @param amounts Configuration des montants par devise
 * @param supportedCurrencies Devises supportées
 * @param majorCurrencies Devises majeures
 * @param defaultCurrency Devise par défaut
 * @param cashLimitEur Limite pour paiements en espèces (EUR uniquement)
 * @param highAmountThreshold Seuil pour devises majeures obligatoires
 * 
 * @version 1.0
 * @since Java 21
 */
@ConfigurationProperties(prefix = "afrostyle.payment")
@Validated
public record PaymentProperties(
    
    @Valid
    @NotNull(message = "Les montants ne peuvent pas être nuls")
    AmountConfiguration amounts,
    
    @NotEmpty(message = "Les devises supportées ne peuvent pas être vides")
    Set<@Pattern(regexp = "^[A-Z]{3}$", message = "Code devise invalide") String> supportedCurrencies,
    
    @NotEmpty(message = "Les devises majeures ne peuvent pas être vides")
    Set<@Pattern(regexp = "^[A-Z]{3}$", message = "Code devise invalide") String> majorCurrencies,
    
    @Pattern(regexp = "^[A-Z]{3}$", message = "Code devise par défaut invalide")
    String defaultCurrency,
    
    @DecimalMin(value = "0.01", message = "La limite d'espèces doit être positive")
    BigDecimal cashLimitEur,
    
    @DecimalMin(value = "0.01", message = "Le seuil de montant élevé doit être positif")
    BigDecimal highAmountThreshold
    
) {
    
    @ConstructorBinding
    public PaymentProperties {
        // Validation personnalisée
        if (!supportedCurrencies.contains(defaultCurrency)) {
            throw new IllegalArgumentException("La devise par défaut doit être dans les devises supportées");
        }
        
        if (!supportedCurrencies.containsAll(majorCurrencies)) {
            throw new IllegalArgumentException("Toutes les devises majeures doivent être supportées");
        }
    }
    
    /**
     * Configuration des montants par devise
     */
    public record AmountConfiguration(
        @DecimalMin(value = "0.01", message = "Le montant minimum doit être positif")
        BigDecimal maxAmount,
        
        @DecimalMin(value = "0.01", message = "Le prix minimum par service doit être positif")
        BigDecimal minPricePerService,
        
        @Valid
        @NotNull(message = "Les montants minimum par devise ne peuvent pas être nuls")  
        Map<String, @DecimalMin(value = "0.01", message = "Montant minimum invalide") BigDecimal> minimumAmounts
    ) {
        
        @ConstructorBinding
        public AmountConfiguration {
            // Validation des montants minimum
            if (minimumAmounts != null) {
                for (var entry : minimumAmounts.entrySet()) {
                    if (!entry.getKey().matches("^[A-Z]{3}$")) {
                        throw new IllegalArgumentException("Code devise invalide: " + entry.getKey());
                    }
                }
            }
        }
    }
    
    /**
     * Obtient le montant minimum pour une devise
     * @param currency Code devise (EUR, USD, etc.)
     * @return Montant minimum ou fallback vers devise par défaut
     */
    public BigDecimal getMinimumAmount(String currency) {
        var amount = amounts.minimumAmounts.get(currency.toUpperCase());
        return amount != null ? amount : amounts.minimumAmounts.get(defaultCurrency);
    }
    
    /**
     * Vérifie si une devise est supportée
     * @param currency Code devise
     * @return true si supportée
     */
    public boolean isSupportedCurrency(String currency) {
        return supportedCurrencies.contains(currency.toUpperCase());
    }
    
    /**
     * Vérifie si une devise est majeure
     * @param currency Code devise
     * @return true si majeure
     */
    public boolean isMajorCurrency(String currency) {
        return majorCurrencies.contains(currency.toUpperCase());
    }
}