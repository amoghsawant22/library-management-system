package com.library.library_management_system.dto.mapper;

import com.library.library_management_system.dto.response.BorrowingHistoryResponse;
import com.library.library_management_system.entity.BorrowingRecord;
import lombok.experimental.UtilityClass;

@UtilityClass
public class BorrowingMapper {

    public static BorrowingHistoryResponse toResponse(BorrowingRecord record) {
        return BorrowingHistoryResponse.builder()
                .id(record.getId())
                .borrowDate(record.getBorrowDate())
                .dueDate(record.getDueDate())
                .returnDate(record.getReturnDate())
                .status(record.getStatus())
                .fineAmount(record.getFineAmount())
                .renewalCount(record.getRenewalCount())
                .maxRenewalsAllowed(record.getMaxRenewalsAllowed())
                .notes(record.getNotes())
                .issuedBy(record.getIssuedBy())
                .returnedTo(record.getReturnedTo())

                // Book details
                .bookId(record.getBook().getId())
                .bookTitle(record.getBook().getTitle())
                .bookAuthor(record.getBook().getAuthor())
                .bookIsbn(record.getBook().getIsbn())

                // User details
                .userId(record.getUser().getId())
                .username(record.getUser().getUsername())
                .userFullName(record.getUser().getFullName())

                // Calculated fields
                .daysOverdue(record.getDaysOverdue())
                .borrowingDuration(record.getBorrowingDuration())
                .canRenew(record.canRenew())
                .isOverdue(record.isOverdue())

                // Audit fields
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }

    public static BorrowingHistoryResponse toUserResponse(BorrowingRecord record) {
        BorrowingHistoryResponse response = toResponse(record);
        // Remove user details for user's own borrowing history
        response.setUserId(null);
        response.setUsername(null);
        response.setUserFullName(null);
        return response;
    }
}