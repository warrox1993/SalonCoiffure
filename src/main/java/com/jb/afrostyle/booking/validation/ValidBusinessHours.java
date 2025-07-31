package com.jb.afrostyle.booking.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Annotation pour valider les heures d'ouverture
 * S'assure que les créneaux sont dans les heures d'ouverture normales
 */
@Documented
@Constraint(validatedBy = BusinessHoursValidatorImpl.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidBusinessHours {

    String message() default "Times must be within business hours (7:00 AM - 10:00 PM)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}