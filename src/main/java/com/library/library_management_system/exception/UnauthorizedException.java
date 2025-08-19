package com.library.library_management_system.exception;

/**
 * Exception thrown when user is not authorized to perform an action
 */
public class UnauthorizedException extends LibraryManagementException {

    public UnauthorizedException(String message) {
        super("UNAUTHORIZED", message);
    }

    public UnauthorizedException(String message, Object... args) {
        super("UNAUTHORIZED", message, args);
    }
}