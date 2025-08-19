package com.library.library_management_system.exception;

/**
 * Exception thrown when an operation is not valid in the current state
 */
public class InvalidOperationException extends LibraryManagementException {

    public InvalidOperationException(String operation, String currentState) {
        super("INVALID_OPERATION",
                "Operation '%s' is not valid in current state: %s", operation, currentState);
    }

    public InvalidOperationException(String message) {
        super("INVALID_OPERATION", message);
    }
}