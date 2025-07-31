package com.jb.afrostyle.core.config.properties;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration des utilisateurs
 * Externalise les constantes liées aux users
 * 
 * @param password Configuration des mots de passe
 * @param profile Configuration des profils utilisateur
 * 
 * @version 1.0
 * @since Java 21
 */
@ConfigurationProperties(prefix = "afrostyle.user")
@Validated
public record UserProperties(
    
    PasswordConfiguration password,
    ProfileConfiguration profile
    
) {
    
    /**
     * Configuration des mots de passe
     */
    public record PasswordConfiguration(
        @Min(value = 6, message = "La longueur minimum du mot de passe doit être d'au moins 6 caractères")
        @Max(value = 256, message = "La longueur maximum du mot de passe ne peut pas dépasser 256 caractères")
        int minLength,
        
        @Min(value = 8, message = "La longueur maximum du mot de passe doit être d'au moins 8 caractères")
        @Max(value = 512, message = "La longueur maximum du mot de passe ne peut pas dépasser 512 caractères")
        int maxLength
    ) {
        
        @ConstructorBinding
        public PasswordConfiguration {
            if (minLength >= maxLength) {
                throw new IllegalArgumentException("La longueur minimum doit être inférieure à la longueur maximum");
            }
        }
    }
    
    /**
     * Configuration des profils utilisateur
     */
    public record ProfileConfiguration(
        @Min(value = 3, message = "La longueur maximum du nom d'utilisateur doit être d'au moins 3 caractères")
        @Max(value = 100, message = "La longueur maximum du nom d'utilisateur ne peut pas dépasser 100 caractères")
        int maxUsernameLength,
        
        @Min(value = 5, message = "La longueur maximum de l'email doit être d'au moins 5 caractères")
        @Max(value = 255, message = "La longueur maximum de l'email ne peut pas dépasser 255 caractères")
        int maxEmailLength,
        
        @Min(value = 2, message = "La longueur maximum du nom complet doit être d'au moins 2 caractères")
        @Max(value = 200, message = "La longueur maximum du nom complet ne peut pas dépasser 200 caractères")
        int maxFullNameLength,
        
        @Min(value = 5, message = "La longueur maximum de la description de paiement doit être d'au moins 5 caractères")
        @Max(value = 500, message = "La longueur maximum de la description de paiement ne peut pas dépasser 500 caractères")
        int maxPaymentDescriptionLength
    ) {}
}