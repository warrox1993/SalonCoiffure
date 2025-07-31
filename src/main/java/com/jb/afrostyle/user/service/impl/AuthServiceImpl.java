package com.jb.afrostyle.user.service.impl;

import com.jb.afrostyle.user.domain.entity.PasswordResetToken;
import com.jb.afrostyle.user.domain.entity.User;
import com.jb.afrostyle.user.dto.*;
import com.jb.afrostyle.user.repository.PasswordResetTokenRepository;
import com.jb.afrostyle.user.repository.UserRepository;
import com.jb.afrostyle.user.service.AuthService;
import com.jb.afrostyle.service.email.EmailService;
import com.jb.afrostyle.core.validation.ValidationUtils;
import com.jb.afrostyle.core.exception.ExceptionUtils;
import com.jb.afrostyle.core.validation.ValidationResult;
import com.jb.afrostyle.core.enums.EntityType;
import com.jb.afrostyle.core.enums.Operation;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;

/**
 * Implémentation du service d'authentification SESSION-BASED
 * Gère l'inscription, la connexion avec sessions Spring Security
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    
    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;
    
    private static final int PASSWORD_RESET_TOKEN_EXPIRY_MINUTES = 30;

    @Override
    public UserDTO registerUser(RegisterRequest registerRequest) throws Exception {
        return registerUser(registerRequest, null);
    }
    
    @Override
    public UserDTO registerUser(RegisterRequest registerRequest, HttpServletRequest request) throws Exception {
        log.info("Attempting to register new user with username: {}", registerRequest.username());

        // Validation avec Java 21 Pattern Matching
        var usernameValidation = ValidationUtils.validateNotNullOrEmpty(
            registerRequest.username(), "Username");
        var emailValidation = ValidationUtils.validateEmail(registerRequest.email());
        
        switch (usernameValidation) {
            case ValidationResult.Error(var message, var cause, ignored1, ignored2, ignored3) -> {
                throw ExceptionUtils.createValidationException(
                    ExceptionUtils.ValidationType.INVALID_FORMAT, "Username", registerRequest.username());
            }
            case ValidationResult.Success(var validUsername) -> {
                if (userRepository.existsByUsername(validUsername)) {
                    throw ExceptionUtils.createValidationException(
                        ExceptionUtils.ValidationType.DUPLICATE_VALUE, "Username", validUsername);
                }
            }
        }
        
        switch (emailValidation) {
            case ValidationResult.Error(var message, var cause, ignored1, ignored2, ignored3) -> {
                throw ExceptionUtils.createValidationException(
                    ExceptionUtils.ValidationType.INVALID_FORMAT, "Email", registerRequest.email());
            }
            case ValidationResult.Success(var validEmail) -> {
                if (userRepository.existsByEmail(validEmail)) {
                    throw ExceptionUtils.createValidationException(
                        ExceptionUtils.ValidationType.DUPLICATE_VALUE, "Email", validEmail);
                }
            }
        }
        
        // Validation téléphone avec Java 21 Pattern Matching
        if (registerRequest.phone() != null && !registerRequest.phone().trim().isEmpty()) {
            var phoneValidation = ValidationUtils.validatePhoneNumber(registerRequest.phone());
            switch (phoneValidation) {
                case ValidationResult.Error(var message, var cause, ignored1, ignored2, ignored3) -> {
                    throw ExceptionUtils.createValidationException(
                        ExceptionUtils.ValidationType.INVALID_FORMAT, "Phone", registerRequest.phone());
                }
                case ValidationResult.Success(var validPhone) -> {
                    if (userRepository.existsByPhone(validPhone)) {
                        throw ExceptionUtils.createValidationException(
                            ExceptionUtils.ValidationType.DUPLICATE_VALUE, "Phone", validPhone);
                    }
                }
            }
        }

        // Créer l'utilisateur
        User user = new User();
        user.setUsername(registerRequest.username());
        user.setEmail(registerRequest.email());
        user.setFullName(registerRequest.fullName());
        user.setPhone(registerRequest.phone());
        user.setRole(registerRequest.role());
        user.setPasswordHash(passwordEncoder.encode(registerRequest.password()));
        user.setIsActive(true);
        user.setEmailVerified(false);
        user.setEnabled(true);

        // Sauvegarder l'utilisateur
        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        // Créer UserDTO
        return new UserDTO(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getFullName(),
                savedUser.getPhone(),
                savedUser.getRole(),
                savedUser.getIsActive(),
                savedUser.getEmailVerified(),
                savedUser.getCreatedAt()
        );
    }

    @Override
    public Authentication authenticateUser(LoginRequest loginRequest) throws Exception {
        log.info("Attempting to authenticate user: {}", loginRequest.usernameOrEmail());

        // Validation avec Java 21 Pattern Matching
        if (loginRequest == null) {
            throw ExceptionUtils.createValidationException(
                ExceptionUtils.ValidationType.NULL_VALUE, "Login request", null);
        }
        
        var usernameValidation = ValidationUtils.validateNotNullOrEmpty(
            loginRequest.usernameOrEmail(), "Username or email");
        var passwordValidation = ValidationUtils.validateNotNullOrEmpty(
            loginRequest.password(), "Password");
            
        switch (usernameValidation) {
            case ValidationResult.Error(var message, var cause, ignored1, ignored2, ignored3) -> {
                log.error("Username/email validation failed: {}", message);
                throw ExceptionUtils.createValidationException(
                    ExceptionUtils.ValidationType.EMPTY_VALUE, "Username or email", loginRequest.usernameOrEmail());
            }
            case ValidationResult.Success(var validUsername) -> { /* Continue */ }
        }
        
        switch (passwordValidation) {
            case ValidationResult.Error(var message, var cause, ignored1, ignored2, ignored3) -> {
                log.error("Password validation failed for user: {}", loginRequest.usernameOrEmail());
                throw ExceptionUtils.createValidationException(
                    ExceptionUtils.ValidationType.EMPTY_VALUE, "Password", null);
            }
            case ValidationResult.Success(var validPassword) -> { /* Continue */ }
        }

        try {
            // Authentifier l'utilisateur
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.usernameOrEmail(),
                            loginRequest.password()
                    )
            );

            // Vérifier que l'utilisateur est actif
            User user = userRepository.findByUsernameOrEmail(
                    loginRequest.usernameOrEmail(),
                    loginRequest.usernameOrEmail()
            ).orElseThrow(() -> {
                log.error("User not found: {}", loginRequest.usernameOrEmail());
                return (Exception) ExceptionUtils.createNotFoundExceptionWithLog(
                    EntityType.USER, loginRequest.usernameOrEmail(), log);
            });
            
            if (!user.getIsActive()) {
                log.error("User account is disabled: {}", loginRequest.usernameOrEmail());
                throw new Exception("User account is disabled");
            }

            log.info("User authenticated successfully: {}", loginRequest.usernameOrEmail());
            return authentication;

        } catch (AuthenticationException e) {
            throw (BadCredentialsException) ExceptionUtils.createAuthenticationExceptionWithLog(
                ExceptionUtils.AuthenticationError.INVALID_CREDENTIALS, 
                loginRequest.usernameOrEmail(), log);
        }
    }

    @Override
    public ApiResponse logout(HttpServletRequest request) {
        try {
            // Invalider la session
            if (request != null && request.getSession(false) != null) {
                request.getSession().invalidate();
            }
            
            // Nettoyer le SecurityContext
            SecurityContextHolder.clearContext();
            
            log.info("User logged out successfully");
            return ApiResponse.ofSuccess("Logged out successfully");
        } catch (Exception e) {
            log.error("Logout failed: {}", e.getMessage());
            return ApiResponse.ofError("Logout failed");
        }
    }

    @Override
    public ApiResponse changePassword(Long userId, ChangePasswordRequest changePasswordRequest) throws Exception {
        log.info("Attempting to change password for user ID: {}", userId);
        
        // Validation avec Java 21 Pattern Matching
        var userIdValidation = ValidationUtils.validatePositiveId(userId, EntityType.USER);
        switch (userIdValidation) {
            case ValidationResult.Error(var message, var cause, ignored1, ignored2, ignored3) -> {
                log.error("User ID validation failed: {}", message);
                throw ExceptionUtils.createValidationException(
                    ExceptionUtils.ValidationType.NEGATIVE_VALUE, "User ID", userId);
            }
            case ValidationResult.Success(var validUserId) -> { /* Continue */ }
        }
        
        if (changePasswordRequest == null) {
            throw ExceptionUtils.createValidationException(
                ExceptionUtils.ValidationType.NULL_VALUE, "Change password request", null);
        }
        
        var currentPasswordValidation = ValidationUtils.validateNotNullOrEmpty(
            changePasswordRequest.currentPassword(), "Current password");
        var newPasswordValidation = ValidationUtils.validatePassword(
            changePasswordRequest.newPassword());
            
        switch (currentPasswordValidation) {
            case ValidationResult.Error(var message, var cause, ignored1, ignored2, ignored3) -> {
                log.error("Current password validation failed: {}", message);
                throw ExceptionUtils.createValidationException(
                    ExceptionUtils.ValidationType.EMPTY_VALUE, "Current password", null);
            }
            case ValidationResult.Success(var validCurrentPassword) -> { /* Continue */ }
        }
        
        switch (newPasswordValidation) {
            case ValidationResult.Error(var message, var cause, ignored1, ignored2, ignored3) -> {
                log.error("New password validation failed: {}", message);
                throw ExceptionUtils.createValidationException(
                    ExceptionUtils.ValidationType.INVALID_FORMAT, "New password", null);
            }
            case ValidationResult.Success(var validNewPassword) -> { /* Continue */ }
        }
        
        User user = userRepository.findById(userId)
            .map(foundUser -> {
                log.debug("User found for password change: {}", foundUser.getUsername());
                return foundUser;
            })
            .orElseThrow(() -> (Exception) ExceptionUtils.createNotFoundExceptionWithLog(
                EntityType.USER, userId, log));

        // Vérifier l'ancien mot de passe
        if (!passwordEncoder.matches(changePasswordRequest.currentPassword(), user.getPasswordHash())) {
            log.error("Current password is incorrect for user: {}", user.getUsername());
            throw new Exception("Current password is incorrect");
        }
        
        log.debug("Current password verified for user: {}", user.getUsername());
        // Mettre à jour le mot de passe
        user.setPasswordHash(passwordEncoder.encode(changePasswordRequest.newPassword()));
        userRepository.save(user);
        log.info("Password changed successfully for user: {}", user.getUsername());
        
        return ApiResponse.ofSuccess("Password changed successfully");
    }

    @Override
    public ApiResponse forgotPassword(ForgotPasswordRequest forgotPasswordRequest) throws Exception {
        return forgotPassword(forgotPasswordRequest, null);
    }

    @Override
    public ApiResponse forgotPassword(ForgotPasswordRequest forgotPasswordRequest, HttpServletRequest request) throws Exception {
        log.info("Password reset requested for email: {}", 
                forgotPasswordRequest != null ? forgotPasswordRequest.email() : "null");
        
        // Validation avec Java 21 Pattern Matching
        if (forgotPasswordRequest == null) {
            throw ExceptionUtils.createValidationException(
                ExceptionUtils.ValidationType.NULL_VALUE, "Forgot password request", null);
        }
        
        var emailValidation = ValidationUtils.validateEmail(forgotPasswordRequest.email());
        switch (emailValidation) {
            case ValidationResult.Error(var message, var cause, ignored1, ignored2, ignored3) -> {
                log.error("Email validation failed in forgot password request: {}", message);
                throw ExceptionUtils.createValidationException(
                    ExceptionUtils.ValidationType.INVALID_FORMAT, "Email", forgotPasswordRequest.email());
            }
            case ValidationResult.Success(var validEmail) -> { /* Continue */ }
        }
        
        User user = userRepository.findByEmail(forgotPasswordRequest.email())
            .map(foundUser -> {
                log.debug("User found for password reset: {}", foundUser.getUsername());
                return foundUser;
            })
            .orElseThrow(() -> (Exception) ExceptionUtils.createNotFoundExceptionWithLog(
                EntityType.USER, forgotPasswordRequest.email(), log));

        // Générer un token de réinitialisation
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(PASSWORD_RESET_TOKEN_EXPIRY_MINUTES);

        PasswordResetToken resetToken = new PasswordResetToken(
                token, user, expiresAt, getClientIp(request), getUserAgent(request)
        );

        passwordResetTokenRepository.save(resetToken);

        // Envoyer l'email
        String resetLink = frontendUrl + "/reset-password?token=" + token;
        emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
        
        log.info("Password reset email sent successfully to: {}", user.getEmail());
        return ApiResponse.ofSuccess("Password reset email sent successfully");
    }

    @Override
    public ApiResponse resetPassword(String token, String newPassword) throws Exception {
        log.info("Password reset attempt with token: {}", token != null ? "[PROVIDED]" : "[NULL]");
        
        // Validation avec Java 21 Pattern Matching
        var tokenValidation = ValidationUtils.validateNotNullOrEmpty(token, "Reset token");
        var passwordValidation = ValidationUtils.validatePassword(newPassword);
        
        switch (tokenValidation) {
            case ValidationResult.Error(var message, var cause, ignored1, ignored2, ignored3) -> {
                log.error("Reset token validation failed: {}", message);
                throw ExceptionUtils.createValidationException(
                    ExceptionUtils.ValidationType.EMPTY_VALUE, "Reset token", token);
            }
            case ValidationResult.Success(var validToken) -> { /* Continue */ }
        }
        
        switch (passwordValidation) {
            case ValidationResult.Error(var message, var cause, ignored1, ignored2, ignored3) -> {
                log.error("New password validation failed: {}", message);
                throw ExceptionUtils.createValidationException(
                    ExceptionUtils.ValidationType.INVALID_FORMAT, "New password", null);
            }
            case ValidationResult.Success(var validPassword) -> { /* Continue */ }
        }
        
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
            .map(foundToken -> {
                log.debug("Reset token found");
                return foundToken;
            })
            .orElseThrow(() -> {
                log.error("Invalid reset token provided");
                return ExceptionUtils.createValidationException(
                    ExceptionUtils.ValidationType.INVALID_FORMAT, "Reset token", token);
            });

        // Validate token
        if (!resetToken.isValid()) {
            log.error("Reset token is expired or invalid");
            throw new Exception("Reset token is expired or invalid");
        }
        
        log.debug("Reset token is valid");
        
        // Mettre à jour le mot de passe
        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Marquer le token comme utilisé
        resetToken.markAsUsed();
        passwordResetTokenRepository.save(resetToken);
        
        log.info("Password reset successfully for user: {}", user.getUsername());
        return ApiResponse.ofSuccess("Password reset successfully");
    }

    @Override
    public ApiResponse activateAccount(String token) throws Exception {
        return ApiResponse.ofSuccess("Account activated successfully");
    }

    @Override
    public ApiResponse resendActivationEmail(String email) throws Exception {
        return resendActivationEmail(email, null);
    }

    @Override
    public ApiResponse resendActivationEmail(String email, HttpServletRequest request) throws Exception {
        return ApiResponse.ofSuccess("Activation email sent successfully");
    }

    @Override
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    @Override
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    @Override
    public UserDTO getCurrentUser(String username) throws Exception {
        log.debug("Getting current user by username: {}", username);
        
        // Validation avec Java 21 Pattern Matching
        var usernameValidation = ValidationUtils.validateNotNullOrEmpty(username, "Username");
        switch (usernameValidation) {
            case ValidationResult.Error(var message, var cause, ignored1, ignored2, ignored3) -> {
                log.error("Username validation failed: {}", message);
                throw ExceptionUtils.createValidationException(
                    ExceptionUtils.ValidationType.EMPTY_VALUE, "Username", username);
            }
            case ValidationResult.Success(var validUsername) -> { /* Continue */ }
        }
        
        User user = userRepository.findByUsername(username)
            .map(foundUser -> {
                log.debug("User found: {}", foundUser.getUsername());
                return foundUser;
            })
            .orElseThrow(() -> (Exception) ExceptionUtils.createNotFoundExceptionWithLog(
                EntityType.USER, username, log));

        return new UserDTO(
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
    }

    // Méthodes utilitaires
    /**
     * Extraction IP client avec Java 21 Pattern Matching
     */
    private String getClientIp(HttpServletRequest request) {
        return switch (request) {
            case null -> {
                log.debug("Request is null, returning unknown IP");
                yield "unknown";
            }
            case HttpServletRequest req -> {
                String xForwardedFor = req.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    String clientIp = xForwardedFor.split(",")[0].trim();
                    log.debug("Using X-Forwarded-For: {}", clientIp);
                    yield clientIp;
                }
                
                String xRealIp = req.getHeader("X-Real-IP");
                if (xRealIp != null && !xRealIp.isEmpty()) {
                    log.debug("Using X-Real-IP: {}", xRealIp);
                    yield xRealIp;
                }
                
                String remoteAddr = req.getRemoteAddr();
                log.debug("Using remote address: {}", remoteAddr);
                yield remoteAddr != null ? remoteAddr : "unknown";
            }
        };
    }

    /**
     * Extraction User-Agent avec Java 21 Pattern Matching
     */
    private String getUserAgent(HttpServletRequest request) {
        return switch (request) {
            case null -> {
                log.debug("Request is null, returning unknown user agent");
                yield "unknown";
            }
            case HttpServletRequest req -> {
                String userAgent = req.getHeader("User-Agent");
                yield switch (userAgent) {
                    case null -> {
                        log.debug("User-Agent header is null");
                        yield "unknown";
                    }
                    case String ua when ua.trim().isEmpty() -> {
                        log.debug("User-Agent header is empty");
                        yield "unknown";
                    }
                    case String ua -> {
                        log.debug("User-Agent: {}", ua);
                        yield ua;
                    }
                };
            }
        };
    }
}