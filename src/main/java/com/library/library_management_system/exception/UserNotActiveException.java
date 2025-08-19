package com.library.library_management_system.exception;

/**
 * Exception thrown when trying to perform operations on inactive user
 */
public class UserNotActiveException extends LibraryManagementException {

    public UserNotActiveException(String username) {
        super("USER_NOT_ACTIVE", "User '%s' is not active", username);
    }

    public UserNotActiveException(Long userId) {
        super("USER_NOT_ACTIVE", "User with ID %d is not active", userId);
    }
}