package com.jb.afrostyle.user.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Annotation pour valider les mots de passe
 */
@Documented
@Constraint(validatedBy = PasswordValidatorImpl.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {

    String message() default "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one digit, and one special character";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}