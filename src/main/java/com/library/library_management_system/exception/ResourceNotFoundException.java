package com.library.library_management_system.exception;

/**
 * Exception thrown when a requested resource is not found
 */
public class ResourceNotFoundException extends LibraryManagementException {

  public ResourceNotFoundException(String message) {
    super("RESOURCE_NOT_FOUND", message);
  }

  public ResourceNotFoundException(String message, Object... args) {
    super("RESOURCE_NOT_FOUND", message, args);
  }

  public ResourceNotFoundException(String resource, String field, Object value) {
    super("RESOURCE_NOT_FOUND", String.format("%s not found with %s: %s", resource, field, value));
  }
}
