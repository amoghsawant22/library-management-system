package com.library.library_management_system.exception;

/**
 * Exception thrown for bad requests or invalid input
 */
public class BadRequestException extends LibraryManagementException {

    public BadRequestException(String message) {
        super("BAD_REQUEST", message);
    }

    public BadRequestException(String message, Object... args) {
        super("BAD_REQUEST", message, args);
    }

    public BadRequestException(String message, Throwable cause) {
        super("BAD_REQUEST", message, cause);
    }
}