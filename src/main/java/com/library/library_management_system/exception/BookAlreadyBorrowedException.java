package com.library.library_management_system.exception;

/**
 * Exception thrown when trying to borrow a book that's already borrowed by the user
 */
public class BookAlreadyBorrowedException extends LibraryManagementException {

    public BookAlreadyBorrowedException(String bookTitle, String username) {
        super("BOOK_ALREADY_BORROWED",
                "Book '%s' is already borrowed by user '%s'", bookTitle, username);
    }

    public BookAlreadyBorrowedException(Long bookId, Long userId) {
        super("BOOK_ALREADY_BORROWED",
                "Book with ID %d is already borrowed by user with ID %d", bookId, userId);
    }
}
