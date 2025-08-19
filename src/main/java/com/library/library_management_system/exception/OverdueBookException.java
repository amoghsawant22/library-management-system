package com.library.library_management_system.exception;

import java.time.LocalDate;

/**
 * Exception thrown for overdue book operations
 */
public class OverdueBookException extends LibraryManagementException {

  public OverdueBookException(String bookTitle, LocalDate dueDate, long daysOverdue) {
    super("BOOK_OVERDUE",
            "Book '%s' is overdue by %d days (due date: %s)",
            bookTitle, daysOverdue, dueDate);
  }

  public OverdueBookException(String message) {
    super("BOOK_OVERDUE", message);
  }
}