package com.library.library_management_system.graphql;

import com.library.library_management_system.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * GraphQL Exception Resolver (Simplified Version)
 */
@Component
@Slf4j
public class GraphQLExceptionResolver {

    /**
     * Handle GraphQL exceptions and convert to user-friendly messages
     */
    public String handleException(Throwable ex) {
        log.error("GraphQL error occurred: {}", ex.getMessage(), ex);

        if (ex instanceof ResourceNotFoundException) {
            return "Resource not found: " + ex.getMessage();
        }

        if (ex instanceof BadRequestException) {
            return "Bad request: " + ex.getMessage();
        }

        if (ex instanceof UnauthorizedException) {
            return "Authentication required";
        }

        if (ex instanceof ForbiddenException) {
            return "Access denied";
        }

        // Handle domain-specific exceptions
        if (ex instanceof BookNotAvailableException ||
                ex instanceof BorrowingLimitExceededException ||
                ex instanceof BookAlreadyBorrowedException ||
                ex instanceof MaxRenewalExceededException) {
            return "Business rule violation: " + ex.getMessage();
        }

        if (ex instanceof DuplicateResourceException) {
            return "Resource already exists: " + ex.getMessage();
        }

        // Default error
        return "Internal server error occurred";
    }

    /**
     * Get error classification
     */
    public String getErrorClassification(Throwable ex) {
        if (ex instanceof ResourceNotFoundException) return "NOT_FOUND";
        if (ex instanceof BadRequestException) return "BAD_REQUEST";
        if (ex instanceof UnauthorizedException) return "UNAUTHORIZED";
        if (ex instanceof ForbiddenException) return "FORBIDDEN";
        if (ex instanceof DuplicateResourceException) return "CONFLICT";

        return "INTERNAL_ERROR";
    }
}