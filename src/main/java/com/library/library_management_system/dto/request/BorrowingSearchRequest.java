package com.library.library_management_system.dto.request;

import com.library.library_management_system.enums.BorrowStatus;
import lombok.*;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowingSearchRequest {

    private Long userId;
    private Long bookId;
    private String username;
    private String bookTitle;
    private String bookAuthor;
    private BorrowStatus status;

    // Date filters
    private LocalDate borrowDateStart;
    private LocalDate borrowDateEnd;
    private LocalDate dueDateStart;
    private LocalDate dueDateEnd;
    private LocalDate returnDateStart;
    private LocalDate returnDateEnd;

    // Special filters
    private Boolean overdueOnly = false;
    private Boolean activeOnly = false; // Currently borrowed
    private Boolean hasFinesToPay = false;

    // Fine filters
    private Double minFineAmount;
    private Double maxFineAmount;

    // Sorting options
    @Pattern(regexp = "^(borrowDate|dueDate|returnDate|status|fineAmount|createdAt)$",
            message = "Invalid sort field")
    @Builder.Default
    private String sortBy = "borrowDate";

    @Pattern(regexp = "^(asc|desc)$", message = "Sort direction must be 'asc' or 'desc'")
    @Builder.Default
    private String sortDirection = "desc";

    // Pagination
    @Min(value = 0, message = "Page must be non-negative")
    @Builder.Default
    private Integer page = 0;

    @Min(value = 1, message = "Size must be at least 1")
    @Builder.Default
    private Integer size = 10;
}
