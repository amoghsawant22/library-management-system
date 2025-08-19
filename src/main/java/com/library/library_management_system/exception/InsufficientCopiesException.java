package com.library.library_management_system.exception;

/**
 * Exception thrown when there are insufficient copies of a book
 */
public class InsufficientCopiesException extends LibraryManagementException {

    public InsufficientCopiesException(String bookTitle, int availableCopies, int requestedCopies) {
        super("INSUFFICIENT_COPIES",
                "Insufficient copies of '%s'. Available: %d, Requested: %d",
                bookTitle, availableCopies, requestedCopies);
    }

    public InsufficientCopiesException(String bookTitle) {
        super("INSUFFICIENT_COPIES", "No copies of '%s' are currently available", bookTitle);
    }
}