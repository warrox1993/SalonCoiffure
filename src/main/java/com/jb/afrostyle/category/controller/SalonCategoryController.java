package com.jb.afrostyle.category.controller;

import com.jb.afrostyle.category.modal.Category;
import com.jb.afrostyle.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST pour la gestion des catégories par les propriétaires de salon
 * Version adaptée pour l'architecture monolithe
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/categories/salon-owner")
public class SalonCategoryController {

    private static final Logger log = LoggerFactory.getLogger(SalonCategoryController.class);

    private final CategoryService categoryService;

    /**
     * Crée une nouvelle catégorie pour le salon
     * MONO-SALON : Plus besoin de salonId car il n'y a qu'un seul salon
     *
     * @param category Données de la catégorie à créer
     * @return Catégorie créée
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SALON_OWNER')")
    public ResponseEntity<?> createCategory(@RequestBody Category category) {
        try {
            log.info("Creating category '{}' for unique salon", category.getName());

            if (category.getName() == null || category.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Category name is required");
            }

            // Plus besoin de validation salon - il n'y en a qu'un
            Category savedCategory = categoryService.saveCategory(category);

            return ResponseEntity.status(HttpStatus.CREATED).body(savedCategory);

        } catch (Exception e) {
            log.error("Error creating category: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Category creation failed: " + e.getMessage());
        }
    }

    /**
     * Supprime une catégorie
     * MONO-SALON : Plus besoin de salonId car il n'y a qu'un seul salon
     *
     * @param categoryId ID de la catégorie à supprimer
     * @return Message de confirmation
     */
    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SALON_OWNER')")
    public ResponseEntity<?> deleteCategory(@PathVariable Long categoryId) {
        try {
            log.info("Deleting category {} from unique salon", categoryId);

            // Validation des paramètres
            if (categoryId == null) {
                return ResponseEntity.badRequest().body("Category ID is required");
            }

            categoryService.deleteCategoryById(categoryId);

            return ResponseEntity.ok("Category deleted successfully");

        } catch (Exception e) {
            log.error("Error deleting category {}: {}", categoryId, e.getMessage());
            return ResponseEntity.badRequest().body("Category deletion failed: " + e.getMessage());
        }
    }

    /**
     * Met à jour une catégorie
     *
     * @param categoryId ID de la catégorie à mettre à jour
     * @param category Nouvelles données de la catégorie
     * @return Catégorie mise à jour
     */
    @PutMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateCategory(
            @PathVariable Long categoryId,
            @RequestBody Category category
    ) {
        try {
            log.info("Updating category with ID: {}", categoryId);

            // Validation des paramètres
            if (categoryId == null) {
                return ResponseEntity.badRequest().body("Category ID is required");
            }

            Category updatedCategory = categoryService.updateCategory(categoryId, category);

            return ResponseEntity.ok(updatedCategory);

        } catch (Exception e) {
            log.error("Error updating category {}: {}", categoryId, e.getMessage());
            return ResponseEntity.badRequest().body("Category update failed: " + e.getMessage());
        }
    }
}