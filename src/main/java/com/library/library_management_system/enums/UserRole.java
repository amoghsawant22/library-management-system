package com.library.library_management_system.enums;

public enum UserRole {
    ADMIN("Admin"),
    MEMBER("Member");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}