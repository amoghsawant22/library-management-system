package com.library.library_management_system.exception;

/**
 * Exception thrown when maximum renewal limit is exceeded
 */
public class MaxRenewalExceededException extends LibraryManagementException {

    public MaxRenewalExceededException(String bookTitle, int currentRenewals, int maxAllowed) {
        super("MAX_RENEWAL_EXCEEDED",
                "Maximum renewal limit exceeded for '%s'. Current renewals: %d, Max allowed: %d",
                bookTitle, currentRenewals, maxAllowed);
    }

    public MaxRenewalExceededException(int maxAllowed) {
        super("MAX_RENEWAL_EXCEEDED",
                "Maximum renewal limit of %d has been reached", maxAllowed);
    }
}