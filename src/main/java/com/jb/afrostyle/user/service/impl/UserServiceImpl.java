package com.jb.afrostyle.user.service.impl;

import com.jb.afrostyle.user.exception.UserException;
import com.jb.afrostyle.user.domain.entity.User;
import com.jb.afrostyle.user.repository.UserRepository;
import com.jb.afrostyle.user.service.UserService;
import com.jb.afrostyle.core.validation.ValidationUtils;
import com.jb.afrostyle.core.exception.ExceptionUtils;
import com.jb.afrostyle.core.validation.ValidationResult;
import com.jb.afrostyle.core.enums.EntityType;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service("userService")
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;

    @Override
    public User createUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public User getUserById(Long id) throws UserException {
        log.debug("Fetching user by ID: {}", id);
        
        // PATTERN MIGRÉ : Validation avec ValidationUtils
        var validationResult = ValidationUtils.validatePositiveId(id, EntityType.USER);
        return switch (validationResult) {
            case ValidationResult.Error(var message, var cause, ignored1, ignored2, ignored3) -> 
                throw new UserException(message);
            case ValidationResult.Success(var validId) -> 
                userRepository.findById(validId)
                    .map(user -> {
                        log.debug("User found: {}", user.getUsername());
                        return user;
                    })
                    .orElseThrow(() -> {
                        log.warn("User not found with ID: {}", validId);
                        return new UserException("User not found with ID: " + validId);
                    });
        };
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public void deleteUser(Long id) throws Exception {
        log.info("Attempting to delete user with ID: {}", id);
        
        // PATTERN MIGRÉ : Validation avec ValidationUtils
        var validationResult = ValidationUtils.validatePositiveId(id, EntityType.USER);
        switch (validationResult) {
            case ValidationResult.Error(var message, var cause, ignored1, ignored2, ignored3) -> 
                throw new Exception(message, cause);
            case ValidationResult.Success(var validId) -> 
                userRepository.findById(validId)
                    .ifPresentOrElse(
                        user -> {
                            log.info("Deleting user: {} (ID: {})", user.getUsername(), user.getId());
                            userRepository.deleteById(user.getId());
                        },
                        () -> {
                            log.error("Cannot delete user - not found with ID: {}", validId);
                            throw new RuntimeException("User not found with ID: " + validId);
                        }
                    );
        }
    }

    @Override
    public User updateUser(long id, User user) throws Exception {
        log.info("Updating user with ID: {}", id);
        
        // PATTERN MIGRÉ : Validation avec ValidationUtils - donnees null
        if (user == null) {
            throw new Exception("Update data cannot be null");
        }
        
        // PATTERN MIGRÉ : Validation avec ValidationUtils - ID
        var validationResult = ValidationUtils.validatePositiveId(id, EntityType.USER);
        return switch (validationResult) {
            case ValidationResult.Error(var message, var cause, ignored1, ignored2, ignored3) -> 
                throw new Exception(message, cause);
            case ValidationResult.Success(var validId) -> {
                User existingUser = userRepository.findById(validId)
                    .map(foundUser -> {
                        log.debug("Updating user: {}", foundUser.getUsername());
                        return foundUser;
                    })
                    .orElseThrow(() -> {
                        log.error("Cannot update user - not found with ID: {}", validId);
                        // PATTERN MIGRÉ : Exception avec ExceptionUtils
                        return ExceptionUtils.createNotFoundException(
                            EntityType.USER, validId
                        );
                    });
                    
                // Update fields conditionally
                if (user.getFullName() != null) {
                    existingUser.setFullName(user.getFullName());
                }
                if (user.getEmail() != null) {
                    existingUser.setEmail(user.getEmail());
                }
                if (user.getRole() != null) {
                    existingUser.setRole(user.getRole());
                }
                if (user.getUsername() != null) {
                    existingUser.setUsername(user.getUsername());
                }
                
                User updatedUser = userRepository.save(existingUser);
                log.info("User updated successfully: {}", updatedUser.getUsername());
                yield updatedUser;
            }
        };
    }

    @Override
    public User findByEmail(String email) {
        log.debug("Finding user by email: {}", email);
        
        // PATTERN MIGRÉ : Validation avec ValidationUtils
        var validationResult = ValidationUtils.validateEmail(email);
        return switch (validationResult) {
            case ValidationResult.Error(var message, var cause, ignored1, ignored2, ignored3) -> {
                log.warn("Cannot find user - {}", message);
                yield null;
            }
            case ValidationResult.Success(var validEmail) -> 
                userRepository.findByEmail(validEmail)
                    .map(user -> {
                        log.debug("User found by email: {}", user.getUsername());
                        return user;
                    })
                    .orElseGet(() -> {
                        log.debug("No user found with email: {}", validEmail);
                        return null;
                    });
        };
    }

    @Override
    public User findByGoogleId(String googleId) {
        log.debug("Finding user by Google ID: {}", googleId);
        
        // PATTERN MIGRÉ : Validation avec ValidationUtils
        var validationResult = ValidationUtils.validateNotNullOrEmpty(googleId, "Google ID");
        return switch (validationResult) {
            case ValidationResult.Error(var message, var cause, ignored1, ignored2, ignored3) -> {
                log.warn("Cannot find user - {}", message);
                yield null;
            }
            case ValidationResult.Success(var validGoogleId) -> 
                userRepository.findByGoogleId(validGoogleId)
                    .map(user -> {
                        log.debug("User found by Google ID: {}", user.getUsername());
                        return user;
                    })
                    .orElseGet(() -> {
                        log.debug("No user found with Google ID: {}", validGoogleId);
                        return null;
                    });
        };
    }

    @Override
    public User findByUsername(String username) {
        log.debug("Finding user by username: {}", username);
        
        // PATTERN MIGRÉ : Validation avec ValidationUtils
        var validationResult = ValidationUtils.validateNotNullOrEmpty(username, "Username");
        return switch (validationResult) {
            case ValidationResult.Error(var message, var cause, ignored1, ignored2, ignored3) -> {
                log.warn("Cannot find user - {}", message);
                yield null;
            }
            case ValidationResult.Success(var validUsername) -> 
                userRepository.findByUsername(validUsername)
                    .map(user -> {
                        log.debug("User found by username: {}", user.getUsername());
                        return user;
                    })
                    .orElseGet(() -> {
                        log.debug("No user found with username: {}", validUsername);
                        return null;
                    });
        };
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }
}