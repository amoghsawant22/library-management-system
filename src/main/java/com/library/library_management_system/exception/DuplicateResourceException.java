package com.library.library_management_system.exception;

/**
 * Exception thrown when trying to create a resource that already exists
 */
public class DuplicateResourceException extends LibraryManagementException {

  public DuplicateResourceException(String resource, String field, Object value) {
    super("DUPLICATE_RESOURCE",
            "%s already exists with %s: %s", resource, field, value);
  }

  public DuplicateResourceException(String message) {
    super("DUPLICATE_RESOURCE", message);
  }
}