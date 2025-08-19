package com.library.library_management_system.graphql;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * GraphQL Error Handler for formatting and sanitizing errors
 */
@Component
@Slf4j
public class GraphQLErrorHandler {

    /**
     * Sanitize errors to remove sensitive information
     */
    public List<GraphQLError> sanitizeErrors(List<GraphQLError> errors) {
        return errors.stream()
                .map(this::sanitizeError)
                .collect(Collectors.toList());
    }

    /**
     * Sanitize individual error
     */
    private GraphQLError sanitizeError(GraphQLError error) {
        log.debug("Sanitizing GraphQL error: {}", error.getMessage());

        String sanitizedMessage = error.getMessage();
        String classification = "UNKNOWN";

        // Categorize and sanitize error messages
        if (error.getExtensions() != null) {
            Object errorCode = error.getExtensions().get("errorCode");

            if ("RESOURCE_NOT_FOUND".equals(errorCode)) {
                classification = "NOT_FOUND";
            } else if ("BAD_REQUEST".equals(errorCode)) {
                classification = "BAD_REQUEST";
            } else if ("UNAUTHORIZED".equals(errorCode)) {
                classification = "UNAUTHORIZED";
                sanitizedMessage = "Authentication required";
            } else if ("FORBIDDEN".equals(errorCode)) {
                classification = "FORBIDDEN";
                sanitizedMessage = "Access denied";
            } else if ("INTERNAL_ERROR".equals(errorCode)) {
                classification = "INTERNAL_ERROR";
                sanitizedMessage = "An internal error occurred";
            }
        }

        // Build sanitized error
        return GraphqlErrorBuilder.newError()
                .message(sanitizedMessage)
                .errorType(error.getErrorType())
                .locations(error.getLocations())
                .path(error.getPath())
                .extensions(Map.of(
                        "classification", classification,
                        "timestamp", Instant.now().toString()
                ))
                .build();
    }

    /**
     * Create user-friendly error message
     */
    public GraphQLError createUserFriendlyError(String message, String classification, List<Object> path) {
        return GraphqlErrorBuilder.newError()
                .message(message)
                .extensions(Map.of(
                        "classification", classification,
                        "userFriendly", true,
                        "timestamp", Instant.now().toString()
                ))
                .path(path)
                .build();
    }

    /**
     * Create validation error for GraphQL
     */
    public GraphQLError createValidationError(String field, String message, Object rejectedValue) {
        return GraphqlErrorBuilder.newError()
                .message(String.format("Validation failed for field '%s': %s", field, message))
                .extensions(Map.of(
                        "classification", "VALIDATION_ERROR",
                        "field", field,
                        "rejectedValue", rejectedValue != null ? rejectedValue.toString() : null,
                        "timestamp", Instant.now().toString()
                ))
                .build();
    }

    /**
     * Check if error is safe to expose to client
     */
    public boolean isSafeError(GraphQLError error) {
        if (error.getExtensions() == null) {
            return false;
        }

        String errorCode = (String) error.getExtensions().get("errorCode");

        // These error types are safe to expose
        return "RESOURCE_NOT_FOUND".equals(errorCode) ||
                "BAD_REQUEST".equals(errorCode) ||
                "VALIDATION_ERROR".equals(errorCode) ||
                "BUSINESS_RULE_VIOLATION".equals(errorCode);
    }

    /**
     * Get error severity level
     */
    public String getErrorSeverity(GraphQLError error) {
        if (error.getExtensions() == null) {
            return "MEDIUM";
        }

        String errorCode = (String) error.getExtensions().get("errorCode");

        return switch (errorCode) {
            case "UNAUTHORIZED", "FORBIDDEN" -> "HIGH";
            case "INTERNAL_ERROR", "SYSTEM_ERROR" -> "CRITICAL";
            case "VALIDATION_ERROR", "BAD_REQUEST" -> "LOW";
            case "RESOURCE_NOT_FOUND" -> "MEDIUM";
            default -> "MEDIUM";
        };
    }
}