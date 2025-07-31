package com.jb.afrostyle.payment.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Annotation pour valider les codes de devise
 * Accepte les codes ISO 4217 standards
 */
@Documented
@Constraint(validatedBy = CurrencyValidatorImpl.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCurrency {

    String message() default "Currency code must be a valid ISO 4217 code (e.g., EUR, USD, GBP)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}