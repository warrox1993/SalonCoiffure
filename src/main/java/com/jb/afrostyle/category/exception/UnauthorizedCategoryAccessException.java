package com.jb.afrostyle.category.exception;

public class UnauthorizedCategoryAccessException extends RuntimeException {
    public UnauthorizedCategoryAccessException(String message) {
        super(message);
    }
    
    public UnauthorizedCategoryAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}