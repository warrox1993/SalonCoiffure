package com.jb.afrostyle.user.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Annotation pour valider les numéros de téléphone
 * Supporte les formats internationaux et nationaux
 */
@Documented
@Constraint(validatedBy = PhoneNumberValidatorImpl.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPhoneNumber {

    String message() default "Phone number must be valid (e.g. +32 475 20 65 25 or 0475 20 65 25)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}