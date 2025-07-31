package com.jb.afrostyle.core.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.function.Function;

/**
 * Utilitaires MapStruct pour la conversion des collections
 * Centralise toutes les opérations de mapping de collections communes
 * Fournit des méthodes optimisées pour les différents types de collections
 * 
 * @version 1.0
 * @since Java 21
 */
@Mapper(componentModel = "spring")
@Component
public class CollectionMapperUtils {
    
    // ==================== CONVERSIONS STRING/COLLECTION ====================
    
    /**
     * Convertit une liste de strings en string délimitée par des virgules
     * @param stringList Liste de strings
     * @return String délimitée par virgules ou null
     */
    @Named("stringListToCommaString")
    public String stringListToCommaString(List<String> stringList) {
        if (stringList == null || stringList.isEmpty()) {
            return null;
        }
        
        return stringList.stream()
                        .filter(Objects::nonNull)
                        .filter(s -> !s.trim().isEmpty())
                        .collect(Collectors.joining(", "));
    }
    
    /**
     * Convertit une string délimitée par des virgules en liste de strings
     * @param commaString String délimitée par virgules
     * @return Liste de strings ou null
     */
    @Named("commaStringToStringList")
    public List<String> commaStringToStringList(String commaString) {
        if (commaString == null || commaString.trim().isEmpty()) {
            return null;
        }
        
        return Arrays.stream(commaString.split(","))
                     .map(String::trim)
                     .filter(s -> !s.isEmpty())
                     .collect(Collectors.toList());
    }
    
    /**
     * Convertit un Set de strings en string délimitée par des virgules
     * @param stringSet Set de strings
     * @return String délimitée par virgules ou null
     */
    @Named("stringSetToCommaString")
    public String stringSetToCommaString(Set<String> stringSet) {
        if (stringSet == null || stringSet.isEmpty()) {
            return null;
        }
        
        return stringSet.stream()
                       .filter(Objects::nonNull)
                       .filter(s -> !s.trim().isEmpty())
                       .sorted() // Pour consistance dans l'ordre
                       .collect(Collectors.joining(", "));
    }
    
    /**
     * Convertit une string délimitée par des virgules en Set de strings
     * @param commaString String délimitée par virgules
     * @return Set de strings ou null
     */
    @Named("commaStringToStringSet")
    public Set<String> commaStringToStringSet(String commaString) {
        if (commaString == null || commaString.trim().isEmpty()) {
            return null;
        }
        
        return Arrays.stream(commaString.split(","))
                     .map(String::trim)
                     .filter(s -> !s.isEmpty())
                     .collect(Collectors.toSet());
    }
    
    // ==================== CONVERSIONS LONG/COLLECTION ====================
    
    /**
     * Convertit une liste de Longs en string délimitée par des virgules
     * @param longList Liste de Longs
     * @return String délimitée par virgules ou null
     */
    @Named("longListToCommaString")
    public String longListToCommaString(List<Long> longList) {
        if (longList == null || longList.isEmpty()) {
            return null;
        }
        
        return longList.stream()
                      .filter(Objects::nonNull)
                      .map(Object::toString)
                      .collect(Collectors.joining(","));
    }
    
    /**
     * Convertit une string délimitée par des virgules en liste de Longs
     * @param commaString String délimitée par virgules
     * @return Liste de Longs ou null
     */
    @Named("commaStringToLongList")
    public List<Long> commaStringToLongList(String commaString) {
        if (commaString == null || commaString.trim().isEmpty()) {
            return null;
        }
        
        return Arrays.stream(commaString.split(","))
                     .map(String::trim)
                     .filter(s -> !s.isEmpty())
                     .map(s -> {
                         try {
                             return Long.parseLong(s);
                         } catch (NumberFormatException e) {
                             return null; // Ignore les valeurs invalides
                         }
                     })
                     .filter(Objects::nonNull)
                     .collect(Collectors.toList());
    }
    
    /**
     * Convertit un Set de Longs en string délimitée par des virgules
     * @param longSet Set de Longs
     * @return String délimitée par virgules ou null
     */
    @Named("longSetToCommaString")
    public String longSetToCommaString(Set<Long> longSet) {
        if (longSet == null || longSet.isEmpty()) {
            return null;
        }
        
        return longSet.stream()
                     .filter(Objects::nonNull)
                     .sorted() // Pour consistance dans l'ordre
                     .map(Object::toString)
                     .collect(Collectors.joining(","));
    }
    
    /**
     * Convertit une string délimitée par des virgules en Set de Longs
     * @param commaString String délimitée par virgules
     * @return Set de Longs ou null
     */
    @Named("commaStringToLongSet")
    public Set<Long> commaStringToLongSet(String commaString) {
        List<Long> longList = commaStringToLongList(commaString);
        return longList != null ? new LinkedHashSet<>(longList) : null;
    }
    
    // ==================== UTILITAIRES DE COLLECTION ====================
    
    /**
     * Vérifie si une collection est vide ou null
     * @param collection Collection à vérifier
     * @return true si vide ou null
     */
    @Named("isEmptyCollection")
    public Boolean isEmptyCollection(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }
    
    /**
     * Obtient la taille d'une collection (0 si null)
     * @param collection Collection à mesurer
     * @return Taille de la collection
     */
    @Named("getCollectionSize")
    public Integer getCollectionSize(Collection<?> collection) {
        return collection != null ? collection.size() : 0;
    }
    
    /**
     * Convertit un Set en List (ordre préservé avec LinkedHashSet)
     * @param set Set à convertir
     * @param <T> Type des éléments
     * @return Liste ou null
     */
    @Named("setToList")
    public <T> List<T> setToList(Set<T> set) {
        if (set == null) {
            return null;
        }
        
        return new ArrayList<>(set);
    }
    
    /**
     * Convertit une List en Set (supprime les doublons)
     * @param list Liste à convertir
     * @param <T> Type des éléments
     * @return Set ou null
     */
    @Named("listToSet")
    public <T> Set<T> listToSet(List<T> list) {
        if (list == null) {
            return null;
        }
        
        return new LinkedHashSet<>(list);
    }
    
    /**
     * Filtre les éléments null d'une liste
     * @param list Liste à filtrer
     * @param <T> Type des éléments
     * @return Liste sans éléments null
     */
    @Named("filterNullFromList")
    public <T> List<T> filterNullFromList(List<T> list) {
        if (list == null) {
            return null;
        }
        
        return list.stream()
                  .filter(Objects::nonNull)
                  .collect(Collectors.toList());
    }
    
    /**
     * Filtre les éléments null d'un set
     * @param set Set à filtrer
     * @param <T> Type des éléments
     * @return Set sans éléments null
     */
    @Named("filterNullFromSet")
    public <T> Set<T> filterNullFromSet(Set<T> set) {
        if (set == null) {
            return null;
        }
        
        return set.stream()
                 .filter(Objects::nonNull)
                 .collect(Collectors.toSet());
    }
    
    // ==================== MAPPPING DE COLLECTIONS AVEC TRANSFORMATION ====================
    
    /**
     * Mappe une liste en appliquant une transformation à chaque élément
     * Utile pour éviter la duplication de code dans les mappers
     * @param list Liste source
     * @param mapper Fonction de transformation
     * @param <S> Type source
     * @param <T> Type cible
     * @return Liste transformée ou null
     */
    @Named("mapList")
    public <S, T> List<T> mapList(List<S> list, Function<S, T> mapper) {
        if (list == null) {
            return null;
        }
        
        return list.stream()
                  .filter(Objects::nonNull)
                  .map(mapper)
                  .filter(Objects::nonNull)
                  .collect(Collectors.toList());
    }
    
    /**
     * Mappe un set en appliquant une transformation à chaque élément
     * @param set Set source
     * @param mapper Fonction de transformation
     * @param <S> Type source
     * @param <T> Type cible
     * @return Set transformé ou null
     */
    @Named("mapSet")
    public <S, T> Set<T> mapSet(Set<S> set, Function<S, T> mapper) {
        if (set == null) {
            return null;
        }
        
        return set.stream()
                 .filter(Objects::nonNull)
                 .map(mapper)
                 .filter(Objects::nonNull)
                 .collect(Collectors.toSet());
    }
    
    // ==================== CONVERSIONS MAP ====================
    
    /**
     * Convertit une Map<String, String> en string formatée
     * Format: "key1=value1,key2=value2"
     * @param map Map à convertir
     * @return String formatée ou null
     */
    @Named("stringMapToString")
    public String stringMapToString(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        
        return map.entrySet().stream()
                 .filter(entry -> entry.getKey() != null && entry.getValue() != null)
                 .map(entry -> entry.getKey() + "=" + entry.getValue())
                 .collect(Collectors.joining(","));
    }
    
    /**
     * Convertit une string formatée en Map<String, String>
     * Format attendu: "key1=value1,key2=value2"
     * @param mapString String formatée
     * @return Map ou null
     */
    @Named("stringToStringMap")
    public Map<String, String> stringToStringMap(String mapString) {
        if (mapString == null || mapString.trim().isEmpty()) {
            return null;
        }
        
        Map<String, String> map = new HashMap<>();
        
        Arrays.stream(mapString.split(","))
              .map(String::trim)
              .filter(s -> !s.isEmpty() && s.contains("="))
              .forEach(pair -> {
                  String[] keyValue = pair.split("=", 2);
                  if (keyValue.length == 2) {
                      map.put(keyValue[0].trim(), keyValue[1].trim());
                  }
              });
        
        return map.isEmpty() ? null : map;
    }
    
    // ==================== UTILITAIRES DE VALIDATION ====================
    
    /**
     * Valide qu'une collection contient au moins un élément
     * @param collection Collection à valider
     * @return true si contient au moins un élément
     */
    @Named("hasElements")
    public Boolean hasElements(Collection<?> collection) {
        return collection != null && !collection.isEmpty();
    }
    
    /**
     * Valide qu'une collection ne dépasse pas une taille maximale
     * @param collection Collection à valider
     * @param maxSize Taille maximale
     * @return true si taille valide
     */
    @Named("isValidSize")
    public Boolean isValidSize(Collection<?> collection, int maxSize) {
        if (collection == null) {
            return true; // null est considéré comme valide
        }
        
        return collection.size() <= maxSize;
    }
    
    /**
     * Compte les éléments non-null dans une collection
     * @param collection Collection à analyser
     * @return Nombre d'éléments non-null
     */
    @Named("countNonNullElements")
    public Integer countNonNullElements(Collection<?> collection) {
        if (collection == null) {
            return 0;
        }
        
        return (int) collection.stream()
                              .filter(Objects::nonNull)
                              .count();
    }
    
    // ==================== MÉTHODES DE SUPPORT POUR BOOKING ====================
    
    /**
     * Convertit une liste d'IDs de services en string pour le système de booking
     * Méthode spécialisée pour les besoins du module booking
     * @param serviceIds Liste des IDs de services
     * @return String au format pour base de données
     */
    @Named("serviceIdsToString")
    public String serviceIdsToString(List<Long> serviceIds) {
        // Utilise la méthode générique mais avec un nom explicite pour les services
        return longListToCommaString(serviceIds);
    }
    
    /**
     * Convertit une string d'IDs de services en liste pour le système de booking
     * @param serviceIdsString String des IDs de services
     * @return Liste d'IDs de services
     */
    @Named("stringToServiceIds")
    public List<Long> stringToServiceIds(String serviceIdsString) {
        // Utilise la méthode générique mais avec un nom explicite pour les services
        return commaStringToLongList(serviceIdsString);
    }
}