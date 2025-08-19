package com.library.library_management_system.exception;

/**
 * Exception thrown when user tries to borrow more books than allowed
 */
public class BorrowingLimitExceededException extends LibraryManagementException {

    public BorrowingLimitExceededException(String username, int currentCount, int maxAllowed) {
        super("BORROWING_LIMIT_EXCEEDED",
                "User '%s' has reached borrowing limit. Current: %d, Max allowed: %d",
                username, currentCount, maxAllowed);
    }

    public BorrowingLimitExceededException(int maxAllowed) {
        super("BORROWING_LIMIT_EXCEEDED", "Maximum borrowing limit of %d books exceeded", maxAllowed);
    }
}