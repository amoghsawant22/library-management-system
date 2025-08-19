package com.library.library_management_system.enums;

public enum BorrowStatus {
    BORROWED("Borrowed"),
    RETURNED("Returned"),
    OVERDUE("Overdue"),
    LOST("Lost"),
    RENEWED("Renewed");

    private final String displayName;

    BorrowStatus(String displayName) {
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