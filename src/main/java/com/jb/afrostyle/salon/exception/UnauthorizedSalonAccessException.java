package com.jb.afrostyle.salon.exception;

public class UnauthorizedSalonAccessException extends RuntimeException {
    public UnauthorizedSalonAccessException(String message) {
        super(message);
    }
    
    public UnauthorizedSalonAccessException(Long salonId, Long userId) {
        super("User " + userId + " is not authorized to access salon " + salonId);
    }
}