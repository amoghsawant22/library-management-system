package com.library.library_management_system.exception;

import lombok.Getter;

/**
 * Base exception for Library Management System
 */
@Getter
public class LibraryManagementException extends RuntimeException {

    private final String errorCode;
    private final Object[] args;

    public LibraryManagementException(String message) {
        super(message);
        this.errorCode = null;
        this.args = null;
    }

    public LibraryManagementException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
        this.args = null;
    }

    public LibraryManagementException(String errorCode, String message, Object... args) {
        super(message);
        this.errorCode = errorCode;
        this.args = args;
    }

    public LibraryManagementException(String errorCode, String message, Throwable cause, Object... args) {
        super(message, cause);
        this.errorCode = errorCode;
        this.args = args;
    }
}