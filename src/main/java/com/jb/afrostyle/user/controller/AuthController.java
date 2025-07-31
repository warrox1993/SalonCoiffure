package com.jb.afrostyle.user.controller;

import com.jb.afrostyle.security.SessionAuthResponse;
import com.jb.afrostyle.security.UserResponse;
import com.jb.afrostyle.user.dto.*;
import com.jb.afrostyle.user.security.CustomUserPrincipal;
import com.jb.afrostyle.user.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;



/**
 * Contrôleur REST pour l'authentification SESSION-BASED
 * Utilise les sessions Spring Security au lieu des JWT
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;

    /**
     * Endpoint d'inscription d'un nouvel utilisateur
     * Accessible publiquement, ne nécessite pas d'authentification
     *
     * @param registerRequest Données d'inscription de l'utilisateur
     * @return Informations de l'utilisateur créé
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest,
                                         jakarta.servlet.http.HttpServletRequest request) {
        try {
            log.info("Registration attempt for username: {}", registerRequest.username());

            UserDTO userDTO = authService.registerUser(registerRequest, request);

            log.info("User registered successfully: {}", registerRequest.username());
            return ResponseEntity.status(HttpStatus.CREATED).body(userDTO);

        } catch (Exception e) {
            log.error("Registration failed for username: {}, error: {}",
                    registerRequest.username(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ofError("Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest,
                                   HttpServletRequest request) {
        try {
            log.info("Login attempt for: {}", loginRequest.usernameOrEmail());

            // 1. Authentifier l'utilisateur
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.usernameOrEmail(),
                            loginRequest.password()
                    )
            );

            // 2. Créer la session Spring Security
            SecurityContextHolder.getContext().setAuthentication(authentication);
            HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());


            // 3. Récupérer les infos utilisateur
            CustomUserPrincipal userPrincipal = (CustomUserPrincipal) authentication.getPrincipal();
            UserDTO userDTO = authService.getCurrentUser(userPrincipal.getUsername());

            // 4. Créer la réponse compatible avec le frontend Angular
            SessionAuthResponse authResponse = new SessionAuthResponse(
                    "SESSION_BASED",
                    "Session",
                    3600L,
                    null,
                    mapToUserResponse(userDTO)
            );

            log.info("Login successful for: {}", loginRequest.usernameOrEmail());
            return ResponseEntity.ok(authResponse);

        } catch (Exception e) {
            log.error("Login failed for: {}, error: {}", loginRequest.usernameOrEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.ofError("Invalid credentials"));
        }
    }


    /**
     * Endpoint de déconnexion
     * Invalide la session Spring Security
     *
     * @return Message de confirmation
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(jakarta.servlet.http.HttpServletRequest request) {
        try {
            log.info("Logout attempt");

            ApiResponse response = authService.logout(request);

            log.info("User logged out successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Logout failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ofError("Logout failed: " + e.getMessage()));
        }
    }

    /**
     * Endpoint de réinitialisation de mot de passe
     * Accessible publiquement, envoie un email avec un lien de réinitialisation
     *
     * @param forgotPasswordRequest Requête contenant l'email de l'utilisateur
     * @return Message de confirmation
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest,
                                           jakarta.servlet.http.HttpServletRequest request) {
        try {
            log.info("Password reset request for email: {}", forgotPasswordRequest.email());

            ApiResponse response = authService.forgotPassword(forgotPasswordRequest, request);

            log.info("Password reset email sent successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Password reset failed for email: {}, error: {}",
                    forgotPasswordRequest.email(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ofError("Password reset failed: " + e.getMessage()));
        }
    }

    /**
     * Endpoint de réinitialisation de mot de passe avec token
     * Accessible publiquement, mais nécessite un token valide
     *
     * @param token Token de réinitialisation
     * @param newPassword Nouveau mot de passe
     * @return Message de confirmation
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String token,
                                          @RequestParam String newPassword) {
        try {
            log.info("Password reset attempt with token");

            ApiResponse response = authService.resetPassword(token, newPassword);

            log.info("Password reset successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Password reset failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ofError("Password reset failed: " + e.getMessage()));
        }
    }

    /**
     * Endpoint de changement de mot de passe
     * Nécessite une authentification (utilisateur connecté en session)
     *
     * @param changePasswordRequest Requête contenant l'ancien et le nouveau mot de passe
     * @return Message de confirmation
     */
    @PostMapping("/change-password")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('SALON_OWNER') or hasRole('ADMIN')")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Pour cette version simplifiée, récupérer l'utilisateur par username
            String username = userDetails.getUsername();
            UserDTO currentUser = authService.getCurrentUser(username);
            Long userId = currentUser.id();

            log.info("Password change attempt for user ID: {}", userId);

            ApiResponse response = authService.changePassword(userId, changePasswordRequest);

            log.info("Password changed successfully for user ID: {}", userId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Password change failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ofError("Password change failed: " + e.getMessage()));
        }
    }

    /**
     * Endpoint de vérification de disponibilité d'un nom d'utilisateur
     * Accessible publiquement
     *
     * @param username Nom d'utilisateur à vérifier
     * @return Statut de disponibilité
     */
    @GetMapping("/check-username/{username}")
    public ResponseEntity<?> checkUsername(@PathVariable String username) {
        try {
            boolean available = authService.isUsernameAvailable(username);
            return ResponseEntity.ok(ApiResponse.ofSuccess("Username " + (available ? "is available" : "is taken")));

        } catch (Exception e) {
            log.error("Username check failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ofError("Username check failed: " + e.getMessage()));
        }
    }

    /**
     * Endpoint de vérification de disponibilité d'un email
     * Accessible publiquement
     *
     * @param email Email à vérifier
     * @return Statut de disponibilité
     */
    @GetMapping("/check-email/{email}")
    public ResponseEntity<?> checkEmail(@PathVariable String email) {
        try {
            boolean available = authService.isEmailAvailable(email);
            return ResponseEntity.ok(ApiResponse.ofSuccess("Email " + (available ? "is available" : "is taken")));

        } catch (Exception e) {
            log.error("Email check failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ofError("Email check failed: " + e.getMessage()));
        }
    }

    /**
     * Endpoint DEBUG pour vérifier l'état de l'authentification
     */
    @GetMapping("/debug")
    public ResponseEntity<?> debugAuth(jakarta.servlet.http.HttpServletRequest request) {
        try {
            org.springframework.security.core.Authentication auth = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            
            return ResponseEntity.ok(java.util.Map.of(
                "hasAuth", auth != null,
                "principal", auth != null ? auth.getName() : "null",
                "authorities", auth != null ? auth.getAuthorities().toString() : "null",
                "sessionId", request.getSession(false) != null ? request.getSession().getId() : "null",
                "authenticated", auth != null ? auth.isAuthenticated() : false
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(java.util.Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint pour récupérer les informations de l'utilisateur connecté
     * Nécessite une authentification (session Spring Security active)
     *
     * @return Informations de l'utilisateur connecté
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.ofError("User not authenticated"));
            }
            
            log.info("Current user info request for: {}", userDetails.getUsername());
            UserDTO userDTO = authService.getCurrentUser(userDetails.getUsername());
            return ResponseEntity.ok(userDTO);

        } catch (Exception e) {
            log.error("Get current user failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.ofError("Failed to get current user: " + e.getMessage()));
        }
    }
    /**
     * Convertit UserDTO en UserResponse pour la compatibilité (flemme... à vérifier)
     */
    private UserResponse mapToUserResponse(UserDTO userDTO) {
        return new UserResponse(
            userDTO.id(),
            userDTO.username(),
            userDTO.email(),
            userDTO.fullName(),
            userDTO.phone(),
            userDTO.role(),
            userDTO.isActive(),
            userDTO.emailVerified(),
            userDTO.createdAt()
        );
    }

}