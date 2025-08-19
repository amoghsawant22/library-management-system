package com.library.library_management_system.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowBookRequest {

    @NotNull(message = "Book ID is required")
    private Long bookId;

    @Positive(message = "Borrowing period must be positive")
    @Builder.Default
    private Integer borrowingPeriodDays = 14; // Default 2 weeks

    @Size(max = 500, message = "Notes must be less than 500 characters")
    private String notes;
}