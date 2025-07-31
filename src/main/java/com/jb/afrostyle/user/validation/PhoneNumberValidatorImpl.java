package com.jb.afrostyle.user.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * Implémentation de la validation des numéros de téléphone
 * Supporte les formats belges et internationaux
 */
public class PhoneNumberValidatorImpl implements ConstraintValidator<ValidPhoneNumber, String> {

    // Pattern pour numéros belges: +32 475 20 65 25 ou 0475 20 65 25
    private static final Pattern BELGIAN_PHONE_PATTERN = Pattern.compile(
            "^(\\+32\\s?|0)([1-9]\\d{1,2})(\\s?\\d{2}){3}$"
    );

    // Pattern général pour numéros internationaux: +XX XXX XXX XXX
    private static final Pattern INTERNATIONAL_PHONE_PATTERN = Pattern.compile(
            "^\\+[1-9]\\d{1,14}$"
    );

    @Override
    public void initialize(ValidPhoneNumber constraintAnnotation) {
        // Aucune initialisation nécessaire
    }

    @Override
    public boolean isValid(String phoneNumber, ConstraintValidatorContext context) {
        // Null est considéré comme valide (utiliser @NotNull si requis)
        if (!StringUtils.hasText(phoneNumber)) {
            return true;
        }

        // Nettoyer le numéro (enlever les espaces)
        String cleanedNumber = phoneNumber.replaceAll("\\s+", "");

        // Vérifier les formats belges
        if (BELGIAN_PHONE_PATTERN.matcher(phoneNumber).matches()) {
            return true;
        }

        // Vérifier le format international
        if (INTERNATIONAL_PHONE_PATTERN.matcher(cleanedNumber).matches()) {
            return true;
        }

        // Vérifier les formats locaux belges sans espaces
        if (cleanedNumber.matches("^0[1-9]\\d{7,8}$")) {
            return true;
        }

        return false;
    }
}