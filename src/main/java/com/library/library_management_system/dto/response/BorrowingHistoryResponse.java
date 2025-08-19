package com.library.library_management_system.dto.response;

import com.library.library_management_system.enums.BorrowStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowingHistoryResponse {

    private Long id;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private BorrowStatus status;
    private Double fineAmount;
    private Integer renewalCount;
    private Integer maxRenewalsAllowed;
    private String notes;
    private String issuedBy;
    private String returnedTo;

    // Book details
    private Long bookId;
    private String bookTitle;
    private String bookAuthor;
    private String bookIsbn;

    // User details (for admin views)
    private Long userId;
    private String username;
    private String userFullName;

    // Calculated fields
    private Long daysOverdue;
    private Long borrowingDuration;
    private Boolean canRenew;
    private Boolean isOverdue;

    // Audit fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}