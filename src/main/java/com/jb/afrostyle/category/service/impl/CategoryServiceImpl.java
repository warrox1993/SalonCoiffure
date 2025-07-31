package com.jb.afrostyle.category.service.impl;

import com.jb.afrostyle.category.exception.CategoryNotFoundException;
import com.jb.afrostyle.category.exception.UnauthorizedCategoryAccessException;
import com.jb.afrostyle.category.modal.Category;
import com.jb.afrostyle.category.repository.CategoryRepository;
import com.jb.afrostyle.category.service.CategoryService;
import com.jb.afrostyle.salon.service.SalonService;
import com.jb.afrostyle.core.validation.ValidationUtils;
import com.jb.afrostyle.core.exception.ExceptionUtils;
import com.jb.afrostyle.core.validation.ValidationResult;
import com.jb.afrostyle.core.enums.EntityType;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Implémentation du service de catégories pour l'architecture monolithe
 * Utilise directement SalonService au lieu d'un client REST
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private static final Logger log = LoggerFactory.getLogger(CategoryServiceImpl.class);

    private final CategoryRepository categoryRepository;
    private final SalonService salonService;

    /**
     * Crée une nouvelle catégorie
     *
     * @param category Catégorie à créer
     * @return Catégorie créée
     */
    @Override
    @Transactional
    public Category saveCategory(Category category) {
        log.info("Creating category '{}'", category.getName());

        try {
            Category newCategory = new Category();
            newCategory.setName(category.getName());
            newCategory.setImages(category.getImages());

            Category savedCategory = categoryRepository.save(newCategory);

            log.info("Category created successfully with ID: {}", savedCategory.getId());

            return savedCategory;

        } catch (Exception e) {
            log.error("Failed to create category: {}", e.getMessage());
            throw new RuntimeException("Failed to create category: " + e.getMessage());
        }
    }

    /**
     * Récupère toutes les catégories
     */
    @Override
    public Set<Category> getAllCategories() {
        log.info("Fetching all categories");
        return Set.copyOf(categoryRepository.findAll());
    }

    /**
     * Récupère une catégorie par son ID
     */
    @Override
    public Category getCategoryById(Long id) throws Exception {
        log.info("Fetching category with ID: {}", id);

        // PATTERN MIGRÉ : Validation avec ValidationUtils
        var validationResult = ValidationUtils.validatePositiveId(id, EntityType.CATEGORY);
        return switch (validationResult) {
            case ValidationResult.Error(var message, var cause, ignored1, ignored2, ignored3) -> 
                throw new Exception(message, cause);
            case ValidationResult.Success(var validId) -> 
                categoryRepository.findById(validId)
                    .orElseThrow(() -> (CategoryNotFoundException) ExceptionUtils.createNotFoundException(
                        EntityType.CATEGORY, validId
                    ));
        };
    }

    /**
     * Supprime une catégorie
     */
    @Override
    @Transactional
    public void deleteCategoryById(Long categoryId) throws Exception {
        log.info("Deleting category {}", categoryId);

        try {
            // PATTERN MIGRÉ : Validation avec ValidationUtils
            var validationResult = ValidationUtils.validatePositiveId(categoryId, EntityType.CATEGORY);
            switch (validationResult) {
                case ValidationResult.Error(var message, var cause, ignored1, ignored2, ignored3) -> 
                    throw new Exception(message, cause);
                case ValidationResult.Success(var validId) -> {
                    // Vérifier que la catégorie existe
                    Category category = categoryRepository.findById(validId)
                            .orElseThrow(() -> (CategoryNotFoundException) ExceptionUtils.createNotFoundException(
                                EntityType.CATEGORY, validId
                            ));

                    // Supprimer la catégorie
                    categoryRepository.deleteById(validId);
                    log.info("Category {} deleted successfully", validId);
                }
            }
        } catch (Exception e) {
            log.error("Failed to delete category {}: {}", categoryId, e.getMessage());
            throw new RuntimeException("Failed to delete category: " + e.getMessage());
        }
    }

    /**
     * Met à jour une catégorie
     */
    @Override
    public Category updateCategory(Long id, Category category) {
        log.info("Updating category with ID: {}", id);

        try {
            // PATTERN MIGRÉ : Validation avec ValidationUtils
            var validationResult = ValidationUtils.validatePositiveId(id, EntityType.CATEGORY);
            return switch (validationResult) {
                case ValidationResult.Error(var message, var cause, ignored1, ignored2, ignored3) -> 
                    throw new RuntimeException(message, cause);
                case ValidationResult.Success(var validId) -> {
                    Category existingCategory = getCategoryById(validId);

                    // Mise à jour des champs modifiables
                    if (category.getName() != null) {
                        existingCategory.setName(category.getName());
                    }
                    if (category.getImages() != null) {
                        existingCategory.setImages(category.getImages());
                    }

                    Category updatedCategory = categoryRepository.save(existingCategory);
                    
                    log.info("Category {} updated successfully", validId);
                    yield updatedCategory;
                }
            };
        } catch (Exception e) {
            log.error("Failed to update category {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to update category: " + e.getMessage());
        }
    }
}