package com.jb.afrostyle.salon.repository;

import com.jb.afrostyle.salon.modal.SalonSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository pour la configuration du salon unique.
 * 
 * MODÈLE MONO-SALON :
 * - Un seul salon par application (id = 1)
 * - Pas de méthodes de recherche complexes
 * - Pas de findBySalonId, findByOwnerId, etc.
 * - Configuration simple et directe
 * 
 * MIGRATION MULTI-SALON → MONO-SALON :
 * - Remplacement de SalonRepository complexe
 * - Suppression de toutes les méthodes multi-salon
 * - Interface simplifiée au maximum
 * 
 * @author AfroStyle Team
 * @since 2.0 (Migration mono-salon)
 */
@Repository
public interface SalonSettingsRepository extends JpaRepository<SalonSettings, Long> {

    /**
     * Récupère la configuration du salon unique.
     * 
     * Cette méthode récupère toujours le salon avec l'ID = 1,
     * car il n'y a qu'un seul salon dans l'application.
     * 
     * @return Configuration du salon unique, ou Optional.empty() si pas configuré
     */
    @Query("SELECT s FROM SalonSettings s WHERE s.id = 1")
    Optional<SalonSettings> findSalonSettings();

    /**
     * Vérifie si le salon est configuré.
     * 
     * @return true si le salon unique existe (id = 1)
     */
    @Query("SELECT COUNT(s) > 0 FROM SalonSettings s WHERE s.id = 1")
    boolean existsSalonSettings();

    /**
     * Récupère le nom du salon unique.
     * 
     * @return Nom du salon ou null si pas configuré
     */
    @Query("SELECT s.name FROM SalonSettings s WHERE s.id = 1")
    Optional<String> findSalonName();

    /**
     * Vérifie si le salon est géolocalisé.
     * 
     * @return true si le salon a des coordonnées GPS
     */
    @Query("SELECT COUNT(s) > 0 FROM SalonSettings s WHERE s.id = 1 AND s.latitude IS NOT NULL AND s.longitude IS NOT NULL")
    boolean isSalonGeolocated();
}

/*
 * NOTES DE MIGRATION :
 * 
 * SUPPRIMÉ de l'ancien SalonRepository :
 * - List<Salon> findByOwnerId(Long ownerId)
 * - List<Salon> findByCity(String city) 
 * - Page<Salon> findAll(Pageable pageable)
 * - Optional<Salon> findByOwnerIdAndId(Long ownerId, Long id)
 * - List<Salon> findByNameContainingIgnoreCase(String name)
 * - + 20 autres méthodes multi-salon
 * 
 * GARDÉ ET SIMPLIFIÉ :
 * - findById(1L) → findSalonSettings()
 * - save() → pour mise à jour configuration
 * - existsById(1L) → existsSalonSettings()
 * 
 * GAIN EN SIMPLICITÉ :
 * - 95% moins de méthodes
 * - Pas de paramètres dynamiques
 * - Pas de risque d'erreur sur les IDs
 * - Cache naturel (un seul enregistrement)
 */