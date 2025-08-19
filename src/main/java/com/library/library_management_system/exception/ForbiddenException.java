package com.library.library_management_system.exception;

/**
 * Exception thrown when user is authenticated but forbidden from accessing resource
 */
public class ForbiddenException extends LibraryManagementException {

  public ForbiddenException(String message) {
    super("FORBIDDEN", message);
  }

  public ForbiddenException(String message, Object... args) {
    super("FORBIDDEN", message, args);
  }
}