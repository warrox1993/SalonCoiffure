package com.jb.afrostyle.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Classe générique pour les réponses d'API
 * Utilisée pour standardiser les réponses de succès et d'erreur
 */
public record ApiResponse(
        /**
         * Indique si l'opération a réussi
         * true = succès, false = erreur
         */
        Boolean success,
        
        /**
         * Message descriptif du résultat de l'opération
         * Peut contenir un message de succès ou d'erreur
         */
        String message,
        
        /**
         * Timestamp de la réponse
         * Inclus uniquement si défini
         */
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime timestamp,
        
        /**
         * Chemin de la requête qui a généré cette réponse
         * Inclus uniquement si défini
         */
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String path,
        
        /**
         * Détails supplémentaires (par exemple, erreurs de validation)
         * Inclus uniquement si défini
         */
        @JsonInclude(JsonInclude.Include.NON_NULL)
        Map<String, Object> details
) {
    /**
     * Constructeur statique pour créer une réponse de succès
     * @param message Message de succès
     * @return Instance d'ApiResponse avec success=true
     */
    public static ApiResponse ofSuccess(String message) {
        return new ApiResponse(true, message, null, null, null);
    }

    /**
     * Constructeur statique pour créer une réponse d'erreur
     * @param message Message d'erreur
     * @return Instance d'ApiResponse avec success=false
     */
    public static ApiResponse ofError(String message) {
        return new ApiResponse(false, message, null, null, null);
    }

    /**
     * Constructeur statique pour créer une réponse de succès générique
     * @return Instance d'ApiResponse avec message de succès par défaut
     */
    public static ApiResponse ofSuccess() {
        return new ApiResponse(true, "Operation completed successfully", null, null, null);
    }

    /**
     * Constructeur statique pour créer une réponse d'erreur générique
     * @return Instance d'ApiResponse avec message d'erreur par défaut
     */
    public static ApiResponse ofError() {
        return new ApiResponse(false, "An error occurred", null, null, null);
    }
}