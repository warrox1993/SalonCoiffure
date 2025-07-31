package com.jb.afrostyle.user.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

/**
 * Implémentation du validateur de mot de passe
 */
@Component
public class PasswordValidatorImpl implements ConstraintValidator<ValidPassword, String> {

    @Override
    public void initialize(ValidPassword constraintAnnotation) {
        // Initialisation si nécessaire
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }

        // Au moins 8 caractères
        if (password.length() < 8) {
            return false;
        }

        // Au moins une majuscule
        if (!password.matches(".*[A-Z].*")) {
            return false;
        }

        // Au moins une minuscule
        if (!password.matches(".*[a-z].*")) {
            return false;
        }

        // Au moins un chiffre
        if (!password.matches(".*[0-9].*")) {
            return false;
        }

        // Au moins un caractère spécial
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>?].*")) {
            return false;
        }

        return true;
    }
}