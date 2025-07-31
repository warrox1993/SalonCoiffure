package com.jb.afrostyle.user.controller;

import com.jb.afrostyle.user.domain.entity.User;
import com.jb.afrostyle.user.dto.UserDTO;
import com.jb.afrostyle.user.service.UserService;
import com.jb.afrostyle.core.validation.ValidationUtils;
import com.jb.afrostyle.core.exception.ExceptionUtils;
import com.jb.afrostyle.core.response.ResponseFactory;
import com.jb.afrostyle.core.validation.ValidationResult;
import com.jb.afrostyle.core.enums.EntityType;
import com.jb.afrostyle.core.enums.EntityType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Contrôleur REST pour la gestion des utilisateurs
 * Version corrigée avec sécurisation des données sensibles
 */
@RestController
@RequiredArgsConstructor
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    /**
     * Crée un nouvel utilisateur
     * Réservé aux administrateurs
     */
    @PostMapping("/api/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUser(@RequestBody @Valid User user) {
        try {
            log.info("Creating new user with username: {}", user.getUsername());

            User createdUser = userService.createUser(user);
            UserDTO userDTO = convertToUserDTO(createdUser);

            log.info("User created successfully with ID: {}", createdUser.getId());
            return ResponseFactory.created(userDTO, "/api/users/" + createdUser.getId());

        } catch (Exception e) {
            log.error("Error creating user: {}", e.getMessage());
            return ResponseFactory.errorFromException(e);
        }
    }

    /**
     * Liste tous les utilisateurs
     * Réservé aux administrateurs
     */
    @GetMapping("/api/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers() {
        try {
            log.info("Admin requesting all users list");

            List<User> users = userService.getAllUsers();
            List<UserDTO> userDTOs = users.stream()
                    .map(this::convertToUserDTO)
                    .collect(Collectors.toList());

            log.info("Returning {} users to admin", userDTOs.size());
            return ResponseFactory.success(userDTOs);

        } catch (Exception e) {
            log.error("Error fetching users list: {}", e.getMessage());
            return ResponseFactory.internalServerError("Failed to fetch users: " + e.getMessage());
        }
    }

    /**
     * SÉCURISÉ : Récupère un utilisateur par son ID
     * Retourne un UserDTO (SANS le passwordHash) au lieu de l'entité User complète
     *
     * Utilisé par les autres modules pour valider l'existence des utilisateurs
     */
    @GetMapping("/api/users/{userId}")
    public ResponseEntity<?> getUser(@PathVariable("userId") Long userId) {
        try {
            log.info("Fetching user with ID: {} for internal communication", userId);

            // PATTERN MIGRÉ : Validation avec ValidationUtils
            var validationResult = ValidationUtils.validatePositiveId(userId, EntityType.USER);
            return switch (validationResult) {
                case ValidationResult.Error(var message, var cause, ignored1, ignored2, ignored3) -> 
                    ResponseFactory.badRequest(message);
                case ValidationResult.Success(var validId) -> {
                    try {
                        // Récupérer l'utilisateur
                        User user = userService.getUserById(validId);
                        
                        // SÉCURITÉ : Convertir en DTO pour masquer les données sensibles
                        UserDTO userDTO = convertToUserDTO(user);
                        
                        log.info("Successfully fetched user: {} for internal use", user.getUsername());
                        yield ResponseFactory.success(userDTO);
                    } catch (Exception e) {
                        log.warn("User not found with ID: {} - {}", validId, e.getMessage());
                        yield ResponseFactory.notFound("User not found with ID: " + validId);
                    }
                }
            };

        } catch (Exception e) {
            log.error("Unexpected error fetching user: {}", e.getMessage());
            return ResponseFactory.errorFromException(e);
        }
    }

    /**
     * Met à jour un utilisateur
     * L'utilisateur peut modifier ses propres données, admin peut modifier tous
     */
    @PutMapping("/api/users/{id}")
    @PreAuthorize("hasRole('ADMIN') or authentication.principal.id == #id")
    public ResponseEntity<?> updateUser(
            @RequestBody User user,
            @PathVariable Long id
    ) {
        try {
            log.info("Updating user with ID: {}", id);

            User updatedUser = userService.updateUser(id, user);
            UserDTO userDTO = convertToUserDTO(updatedUser);

            log.info("User {} updated successfully", id);
            return ResponseFactory.successWithMessage(userDTO, "User updated successfully");

        } catch (Exception e) {
            log.error("Error updating user {}: {}", id, e.getMessage());
            return ResponseFactory.errorFromException(e);
        }
    }

    /**
     * Supprime un utilisateur
     * Réservé aux administrateurs
     */
    @DeleteMapping("/api/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            log.info("Admin deleting user with ID: {}", id);

            userService.deleteUser(id);

            log.info("User {} deleted successfully", id);
            return ResponseFactory.entityResponse(ResponseFactory.EntityOperation.DELETE, "User", id);

        } catch (Exception e) {
            log.error("Error deleting user {}: {}", id, e.getMessage());
            return ResponseFactory.errorFromException(e);
        }
    }

    /**
     * MÉTHODE PRIVÉE : Convertit un User en UserDTO (sans données sensibles)
     * Cette méthode garantit que le passwordHash n'est jamais exposé
     */
    private UserDTO convertToUserDTO(User user) {
        UserDTO userDTO = new UserDTO(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFullName(),
            user.getPhone(),
            user.getRole(),
            user.getIsActive(),
            user.getEmailVerified(),
            user.getCreatedAt()
        );

        // SÉCURITÉ : passwordHash est volontairement EXCLU
        // userDTO.setPasswordHash(user.getPasswordHash()); // ❌ JAMAIS ÇA !

        return userDTO;
    }
}