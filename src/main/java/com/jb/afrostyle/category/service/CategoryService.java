package com.jb.afrostyle.category.service;

import com.jb.afrostyle.category.modal.Category;

import java.util.Set;

/**
 * Interface du service de gestion des catégories
 * Version mono-salon simplifiée
 */
public interface CategoryService {

    /**
     * Sauvegarde une nouvelle catégorie
     * @param category Catégorie à créer
     * @return Catégorie créée
     */
    Category saveCategory(Category category);

    /**
     * Récupère toutes les catégories
     * @return Set des catégories
     */
    Set<Category> getAllCategories();

    /**
     * Récupère une catégorie par son ID
     * @param id ID de la catégorie
     * @return Catégorie trouvée
     * @throws Exception si la catégorie n'existe pas
     */
    Category getCategoryById(Long id) throws Exception;

    /**
     * Supprime une catégorie
     * @param categoryId ID de la catégorie à supprimer
     * @throws Exception si la catégorie n'existe pas
     */
    void deleteCategoryById(Long categoryId) throws Exception;

    /**
     * Met à jour une catégorie
     * @param id ID de la catégorie à mettre à jour
     * @param category Nouvelles données
     * @return Catégorie mise à jour
     */
    Category updateCategory(Long id, Category category);
}