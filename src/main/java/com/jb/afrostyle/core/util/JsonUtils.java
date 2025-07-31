package com.jb.afrostyle.core.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Utilitaires JSON pour AfroStyle
 * Fournit des méthodes sécurisées pour la sérialisation/désérialisation JSON
 * Utilise Jackson avec configuration optimisée pour Spring Boot
 * 
 * @version 1.0
 * @since Java 21
 */
public final class JsonUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);
    
    // ObjectMapper configuré pour l'application
    private static final ObjectMapper OBJECT_MAPPER = createObjectMapper();
    
    /**
     * Constructeur privé pour classe utilitaire
     */
    private JsonUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Crée et configure l'ObjectMapper
     * @return ObjectMapper configuré
     */
    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Support des types Java 8+ (LocalDateTime, etc.)
        mapper.registerModule(new JavaTimeModule());
        
        // Configuration pour éviter les erreurs sur propriétés inconnues
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        // Configuration pour ne pas échec sur propriétés vides
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        
        // Format des dates en ISO
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        return mapper;
    }
    
    // ==================== SÉRIALISATION ====================
    
    /**
     * Convertit un objet en JSON string
     * @param object Objet à sérialiser
     * @return JSON string ou null si erreur
     */
    public static String toJson(Object object) {
        if (object == null) {
            return null;
        }
        
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            logger.error("Error serializing object to JSON: {}", object.getClass().getSimpleName(), e);
            return null;
        }
    }
    
    /**
     * Convertit un objet en JSON string de manière sécurisée
     * @param object Objet à sérialiser
     * @return Optional contenant le JSON ou vide si erreur
     */
    public static Optional<String> toJsonSafe(Object object) {
        return Optional.ofNullable(toJson(object));
    }
    
    /**
     * Convertit un objet en JSON string avec valeur par défaut
     * @param object Objet à sérialiser
     * @param defaultValue Valeur par défaut si erreur
     * @return JSON string ou valeur par défaut
     */
    public static String toJson(Object object, String defaultValue) {
        String result = toJson(object);
        return result != null ? result : defaultValue;
    }
    
    /**
     * Convertit un objet en JSON formaté (pretty print)
     * @param object Objet à sérialiser
     * @return JSON formaté ou null si erreur
     */
    public static String toPrettyJson(Object object) {
        if (object == null) {
            return null;
        }
        
        try {
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            logger.error("Error serializing object to pretty JSON: {}", object.getClass().getSimpleName(), e);
            return null;
        }
    }
    
    // ==================== DÉSÉRIALISATION ====================
    
    /**
     * Convertit un JSON string en objet
     * @param json JSON string
     * @param clazz Classe cible
     * @param <T> Type de l'objet
     * @return Objet désérialisé ou null si erreur
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (StringUtils.isBlank(json) || clazz == null) {
            return null;
        }
        
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            logger.error("Error deserializing JSON to {}: {}", clazz.getSimpleName(), e.getMessage());
            return null;
        }
    }
    
    /**
     * Convertit un JSON string en objet de manière sécurisée
     * @param json JSON string
     * @param clazz Classe cible
     * @param <T> Type de l'objet
     * @return Optional contenant l'objet ou vide si erreur
     */
    public static <T> Optional<T> fromJsonSafe(String json, Class<T> clazz) {
        return Optional.ofNullable(fromJson(json, clazz));
    }
    
    /**
     * Convertit un JSON string en objet avec valeur par défaut
     * @param json JSON string
     * @param clazz Classe cible
     * @param defaultValue Valeur par défaut si erreur
     * @param <T> Type de l'objet
     * @return Objet désérialisé ou valeur par défaut
     */
    public static <T> T fromJson(String json, Class<T> clazz, T defaultValue) {
        T result = fromJson(json, clazz);
        return result != null ? result : defaultValue;
    }
    
    /**
     * Convertit un JSON string en objet avec TypeReference (pour les génériques)
     * @param json JSON string
     * @param typeReference Type reference pour les génériques
     * @param <T> Type de l'objet
     * @return Objet désérialisé ou null si erreur
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        if (StringUtils.isBlank(json) || typeReference == null) {
            return null;
        }
        
        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            logger.error("Error deserializing JSON with TypeReference: {}", e.getMessage());
            return null;
        }
    }
    
    // ==================== COLLECTIONS ====================
    
    /**
     * Convertit un JSON string en List
     * @param json JSON string
     * @param elementClass Classe des éléments de la liste
     * @param <T> Type des éléments
     * @return Liste ou null si erreur
     */
    public static <T> List<T> fromJsonToList(String json, Class<T> elementClass) {
        if (StringUtils.isBlank(json) || elementClass == null) {
            return null;
        }
        
        try {
            return OBJECT_MAPPER.readValue(json, 
                OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, elementClass));
        } catch (JsonProcessingException e) {
            logger.error("Error deserializing JSON to List<{}>: {}", elementClass.getSimpleName(), e.getMessage());
            return null;
        }
    }
    
    /**
     * Convertit un JSON string en Map<String, Object>
     * @param json JSON string
     * @return Map ou null si erreur
     */
    public static Map<String, Object> fromJsonToMap(String json) {
        return fromJson(json, new TypeReference<Map<String, Object>>() {});
    }
    
    /**
     * Convertit un JSON string en Map avec types spécifiés
     * @param json JSON string
     * @param keyClass Classe des clés
     * @param valueClass Classe des valeurs
     * @param <K> Type des clés
     * @param <V> Type des valeurs
     * @return Map ou null si erreur
     */
    public static <K, V> Map<K, V> fromJsonToMap(String json, Class<K> keyClass, Class<V> valueClass) {
        if (StringUtils.isBlank(json) || keyClass == null || valueClass == null) {
            return null;
        }
        
        try {
            return OBJECT_MAPPER.readValue(json, 
                OBJECT_MAPPER.getTypeFactory().constructMapType(Map.class, keyClass, valueClass));
        } catch (JsonProcessingException e) {
            logger.error("Error deserializing JSON to Map<{}, {}>: {}", 
                        keyClass.getSimpleName(), valueClass.getSimpleName(), e.getMessage());
            return null;
        }
    }
    
    // ==================== MANIPULATION JSON ====================
    
    /**
     * Parse un JSON string en JsonNode
     * @param json JSON string
     * @return JsonNode ou null si erreur
     */
    public static JsonNode parseToNode(String json) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        
        try {
            return OBJECT_MAPPER.readTree(json);
        } catch (JsonProcessingException e) {
            logger.error("Error parsing JSON to JsonNode: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Extrait une valeur d'un JSON par chemin
     * @param json JSON string
     * @param path Chemin JSON (ex: "user.name")
     * @return Valeur ou null si non trouvée
     */
    public static String extractValue(String json, String path) {
        JsonNode node = parseToNode(json);
        if (node == null || StringUtils.isBlank(path)) {
            return null;
        }
        
        String[] pathParts = path.split("\\.");
        JsonNode currentNode = node;
        
        for (String part : pathParts) {
            currentNode = currentNode.get(part);
            if (currentNode == null) {
                return null;
            }
        }
        
        return currentNode.isTextual() ? currentNode.asText() : currentNode.toString();
    }
    
    /**
     * Vérifie si un JSON string est valide
     * @param json JSON string à valider
     * @return true si JSON valide
     */
    public static boolean isValidJson(String json) {
        if (StringUtils.isBlank(json)) {
            return false;
        }
        
        try {
            OBJECT_MAPPER.readTree(json);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }
    
    /**
     * Fusionne deux objets JSON
     * @param original Objet original
     * @param update Objet avec mises à jour
     * @param <T> Type de l'objet
     * @return Objet fusionné ou null si erreur
     */
    public static <T> T mergeObjects(T original, T update) {
        if (original == null) {
            return update;
        }
        if (update == null) {
            return original;
        }
        
        try {
            // Convertir en JsonNode
            JsonNode originalNode = OBJECT_MAPPER.valueToTree(original);
            JsonNode updateNode = OBJECT_MAPPER.valueToTree(update);
            
            // Fusionner
            JsonNode mergedNode = merge(originalNode, updateNode);
            
            // Convertir de retour
            @SuppressWarnings("unchecked")
            Class<T> clazz = (Class<T>) original.getClass();
            return OBJECT_MAPPER.treeToValue(mergedNode, clazz);
            
        } catch (Exception e) {
            logger.error("Error merging objects: {}", e.getMessage());
            return original;
        }
    }
    
    /**
     * Fusionne deux JsonNode (récursif)
     * @param original Node original
     * @param update Node de mise à jour
     * @return Node fusionné
     */
    private static JsonNode merge(JsonNode original, JsonNode update) {
        if (update == null) {
            return original;
        }
        
        if (original == null || !original.isObject() || !update.isObject()) {
            return update;
        }
        
        // Créer une copie modifiable
        com.fasterxml.jackson.databind.node.ObjectNode merged = original.deepCopy();
        
        // Fusionner chaque champ
        update.fieldNames().forEachRemaining(fieldName -> {
            JsonNode originalField = original.get(fieldName);
            JsonNode updateField = update.get(fieldName);
            
            if (originalField != null && originalField.isObject() && 
                updateField != null && updateField.isObject()) {
                // Fusion récursive pour les objets
                merged.set(fieldName, merge(originalField, updateField));
            } else {
                // Remplacement direct pour les autres types
                merged.set(fieldName, updateField);
            }
        });
        
        return merged;
    }
    
    // ==================== UTILITAIRES SPÉCIALISÉS ====================
    
    /**
     * Convertit un objet en Map<String, Object>
     * @param object Objet à convertir
     * @return Map représentation de l'objet
     */
    public static Map<String, Object> objectToMap(Object object) {
        if (object == null) {
            return null;
        }
        
        return OBJECT_MAPPER.convertValue(object, new TypeReference<Map<String, Object>>() {});
    }
    
    /**
     * Convertit une Map en objet
     * @param map Map à convertir
     * @param clazz Classe cible
     * @param <T> Type de l'objet
     * @return Objet converti
     */
    public static <T> T mapToObject(Map<String, Object> map, Class<T> clazz) {
        if (map == null || clazz == null) {
            return null;
        }
        
        return OBJECT_MAPPER.convertValue(map, clazz);
    }
    
    /**
     * Clone un objet via sérialisation/désérialisation JSON
     * @param object Objet à cloner
     * @param <T> Type de l'objet
     * @return Clone de l'objet
     */
    @SuppressWarnings("unchecked")
    public static <T> T deepClone(T object) {
        if (object == null) {
            return null;
        }
        
        try {
            String json = OBJECT_MAPPER.writeValueAsString(object);
            return (T) OBJECT_MAPPER.readValue(json, object.getClass());
        } catch (JsonProcessingException e) {
            logger.error("Error deep cloning object: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Obtient la taille en bytes d'un objet sérialisé en JSON
     * @param object Objet à mesurer
     * @return Taille en bytes ou -1 si erreur
     */
    public static long getJsonSize(Object object) {
        String json = toJson(object);
        return json != null ? json.getBytes().length : -1;
    }
    
    /**
     * Compacte un JSON (supprime espaces et indentation)
     * @param json JSON à compacter
     * @return JSON compacté
     */
    public static String compactJson(String json) {
        JsonNode node = parseToNode(json);
        return node != null ? toJson(node) : json;
    }
    
    /**
     * Masque les champs sensibles dans un JSON
     * @param json JSON à masquer
     * @param fieldsToMask Champs à masquer
     * @return JSON masqué
     */
    public static String maskSensitiveFields(String json, String... fieldsToMask) {
        JsonNode node = parseToNode(json);
        if (node == null || fieldsToMask == null) {
            return json;
        }
        
        try {
            com.fasterxml.jackson.databind.node.ObjectNode objNode = (com.fasterxml.jackson.databind.node.ObjectNode) node;
            
            for (String field : fieldsToMask) {
                if (objNode.has(field)) {
                    objNode.put(field, "***");
                }
            }
            
            return OBJECT_MAPPER.writeValueAsString(objNode);
        } catch (Exception e) {
            logger.error("Error masking sensitive fields: {}", e.getMessage());
            return json;
        }
    }
    
    /**
     * Obtient l'ObjectMapper configuré
     * @return ObjectMapper instance
     */
    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }
}