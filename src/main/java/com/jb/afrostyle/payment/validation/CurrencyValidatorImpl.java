package com.jb.afrostyle.payment.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

import java.util.Set;

/**
 * Implémentation de la validation des codes de devise
 * Vérifie que le code correspond à un code ISO 4217 valide
 */
public class CurrencyValidatorImpl implements ConstraintValidator<ValidCurrency, String> {

    // Codes de devise les plus courants supportés
    private static final Set<String> SUPPORTED_CURRENCIES = Set.of(
            "EUR", "USD", "GBP", "JPY", "CHF", "CAD", "AUD", "NZD", "SEK", "NOK", "DKK", "PLN", "CZK", "HUF"
    );

    @Override
    public void initialize(ValidCurrency constraintAnnotation) {
        // Aucune initialisation nécessaire
    }

    @Override
    public boolean isValid(String currency, ConstraintValidatorContext context) {
        // Null est considéré comme valide si non requis
        if (!StringUtils.hasText(currency)) {
            return true;
        }

        // Vérifier que le code est en majuscules et fait 3 caractères
        if (currency.length() != 3) {
            return false;
        }

        // Vérifier que le code est dans la liste des devises supportées
        return SUPPORTED_CURRENCIES.contains(currency.toUpperCase());
    }
}