package com.jb.afrostyle.user.repository;

import com.jb.afrostyle.user.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Trouve un utilisateur par nom d'utilisateur
     */
    Optional<User> findByUsername(String username);

    /**
     * Trouve un utilisateur par email
     */
    Optional<User> findByEmail(String email);

    /**
     * Trouve un utilisateur par nom d'utilisateur OU email
     */
    Optional<User> findByUsernameOrEmail(String username, String email);

    /**
     * Vérifie si un nom d'utilisateur existe
     */
    boolean existsByUsername(String username);

    /**
     * Vérifie si un email existe
     */
    boolean existsByEmail(String email);

    /**
     * Vérifie si un numéro de téléphone existe
     */
    boolean existsByPhone(String phone);

    /**
     * Trouve un utilisateur par Google ID
     */
    Optional<User> findByGoogleId(String googleId);

    /**
     * Vérifie si un Google ID existe
     */
    boolean existsByGoogleId(String googleId);
}