package com.jb.afrostyle.core.exception;

import com.jb.afrostyle.core.dto.ApiResponse;
import com.jb.afrostyle.core.response.ResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Gestionnaire global d'exceptions centralisé pour AfroStyle
 * Utilise Java 21 Pattern Matching et ResponseFactory pour des réponses cohérentes
 * Intègre logging de sécurité et gestion d'erreurs avancée
 * 
 * @version 1.0
 * @since Java 21
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    // ==================== EXCEPTIONS MÉTIER ====================
    
    /**
     * Gestion des BusinessException avec Pattern Matching
     * @param ex BusinessException à traiter
     * @param request Requête HTTP
     * @return ResponseEntity avec détails d'erreur
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {
        
        logBusinessException(ex, request);
        
        // Détermine le status HTTP selon le code d'erreur
        HttpStatus status = switch (ex.getErrorCode()) {
            case "NOT_FOUND", "USER_NOT_FOUND", "BOOKING_NOT_FOUND", 
                 "SALON_NOT_FOUND", "PAYMENT_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "UNAUTHORIZED", "ACCESS_DENIED" -> HttpStatus.FORBIDDEN;
            case "VALIDATION_ERROR", "INVALID_STATE" -> HttpStatus.BAD_REQUEST;
            case "CONFLICT", "BOOKING_CONFLICT", "PAYMENT_PROCESSED" -> HttpStatus.CONFLICT;
            case "CONSTRAINT_VIOLATION" -> HttpStatus.UNPROCESSABLE_ENTITY;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
        
        ApiResponse response = ApiResponse.of(
            false,
            ex.getMessage(),
            ex.getErrorCode(),
            request.getRequestURI(),
            ex.getContext(),
            ex.getOperation(),
            ex.getEntityType()
        );
        
        return ResponseEntity.status(status).body(response);
    }
    
    /**
     * Gestion des ValidationException avec détails des champs
     * @param ex ValidationException à traiter
     * @param request Requête HTTP
     * @return ResponseEntity avec détails de validation
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse> handleValidationException(
            ValidationException ex, HttpServletRequest request) {
        
        logger.warn("Validation error on {}: {} [Fields: {}]", 
                   request.getRequestURI(), ex.getMessage(), ex.getFieldErrors().keySet());
        
        Map<String, Object> details = new HashMap<>();
        details.put("fieldErrors", ex.getFieldErrors());
        details.put("errorCount", ex.getFieldErrorCount());
        
        if (ex.getFieldName() != null) {
            details.put("primaryField", ex.getFieldName());
            details.put("primaryValue", ex.getFieldValue());
        }
        
        ApiResponse response = ApiResponse.of(
            false,
            ex.hasFieldErrors() ? "Validation failed for multiple fields" : ex.getMessage(),
            ex.getErrorCode(),
            request.getRequestURI(),
            details,
            null,
            null
        );
        
        return ResponseEntity.badRequest().body(response);
    }
    
    // ==================== EXCEPTIONS DE SÉCURITÉ ====================
    
    /**
     * Gestion des exceptions d'authentification avec sécurité renforcée
     * @param ex BadCredentialsException
     * @param request Requête HTTP
     * @return ResponseEntity sécurisée
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse> handleBadCredentialsException(
            BadCredentialsException ex, HttpServletRequest request) {
        
        // Log de sécurité avec détails pour audit
        logger.error("SECURITY ALERT: Authentication failure on {} from IP {} [User-Agent: {}]",
                    request.getRequestURI(),
                    getClientIpAddress(request),
                    request.getHeader("User-Agent"));
        
        // Message générique pour éviter l'énumération d'utilisateurs
        ApiResponse response = ApiResponse.ofAuthenticationError(
            "Authentication failed. Please check your credentials."
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response.withPath(request.getRequestURI()));
    }
    
    /**
     * Gestion des exceptions d'autorisation
     * @param ex AccessDeniedException
     * @param request Requête HTTP
     * @return ResponseEntity d'autorisation
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {
        
        logger.error("SECURITY ALERT: Access denied on {} from IP {} [Message: {}]",
                    request.getRequestURI(),
                    getClientIpAddress(request),
                    ex.getMessage());
        
        ApiResponse response = ApiResponse.ofAuthorizationError(
            "Access denied. You don't have permission to access this resource."
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response.withPath(request.getRequestURI()));
    }
    
    // ==================== EXCEPTIONS DE VALIDATION SPRING ====================
    
    /**
     * Gestion des erreurs de validation Bean Validation
     * @param ex MethodArgumentNotValidException
     * @param request Requête HTTP
     * @return ResponseEntity avec erreurs de validation
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        Map<String, String> fieldErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                error -> error.getField(),
                error -> error.getDefaultMessage(),
                (existing, replacement) -> existing
            ));
        
        logger.warn("Bean validation failed on {}: {} field errors", 
                   request.getRequestURI(), fieldErrors.size());
        
        ApiResponse response = ApiResponse.ofValidationError(Map.of(
            "fieldErrors", fieldErrors,
            "objectName", ex.getBindingResult().getObjectName(),
            "errorCount", fieldErrors.size()
        ));
        
        return ResponseEntity.badRequest().body(response.withPath(request.getRequestURI()));
    }
    
    /**
     * Gestion des erreurs de liaison (bind) de données
     * @param ex BindException
     * @param request Requête HTTP
     * @return ResponseEntity avec erreurs de liaison
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse> handleBindException(
            BindException ex, HttpServletRequest request) {
        
        Map<String, String> fieldErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                error -> error.getField(),
                error -> error.getDefaultMessage(),
                (existing, replacement) -> existing
            ));
        
        logger.warn("Data binding failed on {}: {}", request.getRequestURI(), fieldErrors);
        
        ApiResponse response = ApiResponse.ofValidationError(Map.of("fieldErrors", fieldErrors));
        
        return ResponseEntity.badRequest().body(response.withPath(request.getRequestURI()));
    }
    
    /**
     * Gestion des violations de contraintes
     * @param ex ConstraintViolationException
     * @param request Requête HTTP
     * @return ResponseEntity avec violations
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse> handleConstraintViolationException(
            ConstraintViolationException ex, HttpServletRequest request) {
        
        Map<String, String> violations = ex.getConstraintViolations()
            .stream()
            .collect(Collectors.toMap(
                violation -> violation.getPropertyPath().toString(),
                violation -> violation.getMessage(),
                (existing, replacement) -> existing
            ));
        
        logger.warn("Constraint violations on {}: {}", request.getRequestURI(), violations);
        
        ApiResponse response = ApiResponse.ofValidationError(Map.of("violations", violations));
        
        return ResponseEntity.badRequest().body(response.withPath(request.getRequestURI()));
    }
    
    // ==================== EXCEPTIONS HTTP/WEB ====================
    
    /**
     * Gestion des erreurs de paramètres manquants
     * @param ex MissingServletRequestParameterException
     * @param request Requête HTTP
     * @return ResponseEntity pour paramètre manquant
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        
        logger.warn("Missing request parameter '{}' of type '{}' on {}", 
                   ex.getParameterName(), ex.getParameterType(), request.getRequestURI());
        
        ApiResponse response = ApiResponse.ofError(
            String.format("Required parameter '%s' is missing", ex.getParameterName()),
            "MISSING_PARAMETER"
        );
        
        return ResponseEntity.badRequest().body(response.withPath(request.getRequestURI()));
    }
    
    /**
     * Gestion des erreurs de type d'argument
     * @param ex MethodArgumentTypeMismatchException
     * @param request Requête HTTP
     * @return ResponseEntity pour type incorrect
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        
        logger.warn("Type mismatch for parameter '{}': expected '{}', got '{}'", 
                   ex.getName(), ex.getRequiredType().getSimpleName(), ex.getValue());
        
        ApiResponse response = ApiResponse.ofError(
            String.format("Invalid value '%s' for parameter '%s'. Expected type: %s", 
                         ex.getValue(), ex.getName(), ex.getRequiredType().getSimpleName()),
            "TYPE_MISMATCH"
        );
        
        return ResponseEntity.badRequest().body(response.withPath(request.getRequestURI()));
    }
    
    /**
     * Gestion des erreurs de lecture de message HTTP
     * @param ex HttpMessageNotReadableException
     * @param request Requête HTTP
     * @return ResponseEntity pour message non lisible
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        
        logger.warn("HTTP message not readable on {}: {}", request.getRequestURI(), ex.getMessage());
        
        String message = ex.getMessage().contains("JSON") 
            ? "Invalid JSON format in request body"
            : "Request body is not readable";
        
        ApiResponse response = ApiResponse.ofError(message, "INVALID_REQUEST_BODY");
        
        return ResponseEntity.badRequest().body(response.withPath(request.getRequestURI()));
    }
    
    /**
     * Gestion des méthodes HTTP non supportées
     * @param ex HttpRequestMethodNotSupportedException
     * @param request Requête HTTP
     * @return ResponseEntity pour méthode non supportée
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        
        logger.warn("Method '{}' not supported for {}", ex.getMethod(), request.getRequestURI());
        
        ApiResponse response = ApiResponse.ofError(
            String.format("Method '%s' not supported. Supported methods: %s", 
                         ex.getMethod(), String.join(", ", ex.getSupportedMethods())),
            "METHOD_NOT_SUPPORTED"
        );
        
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response.withPath(request.getRequestURI()));
    }
    
    /**
     * Gestion des ressources non trouvées (404)
     * @param ex NoHandlerFoundException
     * @param request Requête HTTP
     * @return ResponseEntity pour ressource non trouvée
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse> handleNoHandlerFoundException(
            NoHandlerFoundException ex, HttpServletRequest request) {
        
        logger.warn("No handler found for {} {}", ex.getHttpMethod(), ex.getRequestURL());
        
        ApiResponse response = ApiResponse.ofError(
            String.format("No handler found for %s %s", ex.getHttpMethod(), ex.getRequestURL()),
            "ENDPOINT_NOT_FOUND"
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response.withPath(request.getRequestURI()));
    }
    
    // ==================== EXCEPTION GÉNÉRIQUE ====================
    
    /**
     * Gestionnaire d'exception générique (fallback)
     * @param ex Exception non gérée spécifiquement
     * @param request Requête HTTP
     * @return ResponseEntity générique
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        
        // Log complet pour les erreurs inattendues
        logger.error("Unexpected error on {} from IP {}: {}", 
                    request.getRequestURI(),
                    getClientIpAddress(request),
                    ex.getMessage(), ex);
        
        // Message générique pour éviter la fuite d'informations
        ApiResponse response = ApiResponse.ofError(
            "An unexpected error occurred. Please try again later.",
            "INTERNAL_ERROR"
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body(response.withPath(request.getRequestURI()));
    }
    
    // ==================== MÉTHODES UTILITAIRES ====================
    
    /**
     * Log une BusinessException avec contexte approprié
     * @param ex BusinessException
     * @param request Requête HTTP
     */
    private void logBusinessException(BusinessException ex, HttpServletRequest request) {
        if (ex.hasErrorCode("NOT_FOUND")) {
            logger.warn("Business error on {}: {} [Entity: {}, Operation: {}]",
                       request.getRequestURI(), ex.getMessage(),
                       ex.getEntityType(), ex.getOperation());
        } else {
            logger.error("Business error on {}: {} [Context: {}]",
                        request.getRequestURI(), ex.getMessage(), ex.getContext(), ex);
        }
    }
    
    /**
     * Obtient l'adresse IP réelle du client
     * @param request Requête HTTP
     * @return Adresse IP du client
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}