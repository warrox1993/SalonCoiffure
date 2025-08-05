package com.jb.afrostyle.user.service;

import com.jb.afrostyle.user.exception.UserException;
import com.jb.afrostyle.user.domain.entity.User;
import java.util.List;

public interface UserService{
    User createUser (User user);
    User getUserById (Long id) throws UserException;
    List<User> getAllUsers();
    void deleteUser(Long id) throws Exception;
    User updateUser (long id, User user) throws Exception;
    User findByEmail(String email);
    User findByGoogleId(String googleId);
    User findByUsername(String username);
    User save(User user);
    
    // Méthodes JPA optimisées ajoutées dans l'implémentation
    User getUserWithProfile(Long id) throws UserException;
    User getUserWithFavorites(Long id) throws UserException;
    List<User> getAllUsersWithProfiles();
}