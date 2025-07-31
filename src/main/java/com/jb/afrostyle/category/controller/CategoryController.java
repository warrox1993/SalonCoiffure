package com.jb.afrostyle.category.controller;

import com.jb.afrostyle.category.modal.Category;
import com.jb.afrostyle.category.service.CategoryService;
import com.jb.afrostyle.core.validation.ValidationUtils;
import com.jb.afrostyle.core.exception.ExceptionUtils;
import com.jb.afrostyle.core.response.ResponseFactory;
import com.jb.afrostyle.core.validation.ValidationResult;
import com.jb.afrostyle.core.enums.EntityType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private static final Logger log = LoggerFactory.getLogger(CategoryController.class);
    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<?> getAllCategories() {
        try {
            log.info("Fetching all categories");
            // MONO-SALON: Récupérer toutes les catégories
            Set<Category> categories = categoryService.getAllCategories();
            return ResponseFactory.success(categories);
        } catch (Exception e) {
            log.error("Error fetching all categories: {}", e.getMessage());
            return ResponseFactory.errorFromException(e);
        }
    }
    
    @GetMapping("/salon/{id}")
    public ResponseEntity<?> getCategoriesBySalon(
            @PathVariable Long id
    ){
        try {
            // PATTERN MIGRÉ : Validation avec ValidationUtils
            var validationResult = ValidationUtils.validatePositiveId(id, EntityType.SALON);
            return switch (validationResult) {
                case ValidationResult.Error(var message, var cause, ignored1, ignored2, ignored3) -> 
                    ResponseFactory.badRequest(message);
                case ValidationResult.Success(var validId) -> {
                    log.info("Fetching categories for salon {} (MONO-SALON: returning all)", validId);
                    // MONO-SALON: Redirige vers getAllCategories pour compatibilité
                    yield getAllCategories();
                }
            };
        } catch (Exception e) {
            log.error("Error fetching categories for salon: {}", e.getMessage());
            return ResponseFactory.errorFromException(e);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCategoriesById(
            @PathVariable Long id
    ) {
        try {
            // PATTERN MIGRÉ : Validation avec ValidationUtils
            var validationResult = ValidationUtils.validatePositiveId(id, EntityType.CATEGORY);
            return switch (validationResult) {
                case ValidationResult.Error(var message, var cause, ignored1, ignored2, ignored3) -> 
                    ResponseFactory.badRequest(message);
                case ValidationResult.Success(var validId) -> {
                    try {
                        log.info("Fetching category with ID: {}", validId);
                        Category category = categoryService.getCategoryById(validId);
                        yield ResponseFactory.success(category);
                    } catch (Exception e) {
                        log.error("Category not found with ID: {}", validId);
                        // PATTERN MIGRÉ : Exception avec ExceptionUtils
                        yield ResponseFactory.errorFromException(
                            ExceptionUtils.createNotFoundException(EntityType.CATEGORY, validId)
                        );
                    }
                }
            };
        } catch (Exception e) {
            log.error("Unexpected error fetching category: {}", e.getMessage());
            return ResponseFactory.errorFromException(e);
        }
    }
}