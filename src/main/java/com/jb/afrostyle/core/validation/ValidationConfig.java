package com.jb.afrostyle.core.validation;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import jakarta.validation.Validator;

/**
 * Configuration centralisée pour la validation dans AfroStyle
 * Intègre Bean Validation (JSR-303) avec notre système de validation personnalisé
 * 
 * @version 1.0
 * @since Java 21
 */
@Configuration
public class ValidationConfig {
    
    /**
     * Bean pour la validation standard JSR-303
     * @return ValidatorFactory configurée
     */
    @Bean
    public LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean();
    }
    
    /**
     * Post-processor pour la validation des méthodes
     * Permet l'utilisation de @Valid sur les paramètres de méthodes
     * @return MethodValidationPostProcessor configuré
     */
    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        MethodValidationPostProcessor processor = new MethodValidationPostProcessor();
        processor.setValidator(validator());
        return processor;
    }
    
    /**
     * Registre de validateurs personnalisés
     * Bean principal pour la validation centralisée
     * @return ValidatorRegistry configuré
     */
    @Bean
    public ValidatorRegistry validatorRegistry() {
        return new ValidatorRegistry();
    }
}