package com.jb.afrostyle.salon.exception;

public class SalonNotFoundException extends RuntimeException {
    public SalonNotFoundException(String message) {
        super(message);
    }
    
    public SalonNotFoundException(Long salonId) {
        super("Salon not found with id: " + salonId);
    }
}