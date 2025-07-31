package com.jb.afrostyle.category.modal;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Catégorie de services pour le salon unique.
 * 
 * MODÈLE MONO-SALON :
 * - Plus de salonId (toutes les catégories appartiennent au salon unique)
 * - Catégories globales pour l'application
 * - Simplification de la logique métier
 * 
 * MIGRATION MULTI-SALON → MONO-SALON :
 * - Suppression salonId (champ obligatoire avant)
 * - Suppression relations JPA vers Salon
 * - Plus de validation ownership
 * 
 * @author AfroStyle Team
 * @since 2.0 (Migration mono-salon)
 */
@Entity
@Data
@Table(name = "categories", indexes = {
    @Index(name = "idx_categories_name", columnList = "name")
})
public class Category {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "Le nom de la catégorie est obligatoire")
    private String name;

    /**
     * Description de la catégorie.
     */
    @Column(length = 500)
    private String description;

    /**
     * URLs des images de la catégorie (séparées par des virgules).
     */
    @Column(length = 1000)
    private String images;

    /**
     * Couleur associée à la catégorie (hex code).
     */
    @Column(length = 7)
    private String color;

    /**
     * Ordre d'affichage de la catégorie.
     */
    private Integer displayOrder = 0;

    /**
     * Indique si la catégorie est active.
     */
    @Column(nullable = false)
    private Boolean active = true;

    /**
     * Date de création de la catégorie.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Date de dernière mise à jour de la catégorie.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}