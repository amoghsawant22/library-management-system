package com.library.library_management_system.exception;

/**
 * Exception thrown when a book is not available for borrowing
 */
public class BookNotAvailableException extends LibraryManagementException {

    public BookNotAvailableException(String bookTitle) {
        super("BOOK_NOT_AVAILABLE", "Book '%s' is not available for borrowing", bookTitle);
    }

    public BookNotAvailableException(String bookTitle, String reason) {
        super("BOOK_NOT_AVAILABLE", "Book '%s' is not available: %s", bookTitle, reason);
    }

    public BookNotAvailableException(Long bookId, String reason) {
        super("BOOK_NOT_AVAILABLE", "Book with ID %d is not available: %s", bookId, reason);
    }
}