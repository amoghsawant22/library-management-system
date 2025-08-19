package com.library.library_management_system.exception;

/**
 * Exception thrown when trying to return a book that's already returned
 */
public class BookAlreadyReturnedException extends LibraryManagementException {

  public BookAlreadyReturnedException(String bookTitle) {
    super("BOOK_ALREADY_RETURNED", "Book '%s' has already been returned", bookTitle);
  }

  public BookAlreadyReturnedException(Long borrowingRecordId) {
    super("BOOK_ALREADY_RETURNED",
            "Book in borrowing record %d has already been returned", borrowingRecordId);
  }
}