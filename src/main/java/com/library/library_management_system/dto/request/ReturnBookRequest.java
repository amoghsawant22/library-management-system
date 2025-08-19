package com.library.library_management_system.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnBookRequest {

    @NotNull(message = "Borrowing record ID is required")
    private Long borrowingRecordId;

    @Size(max = 500, message = "Notes must be less than 500 characters")
    private String notes;

    private Boolean isLost = false;

    private Boolean isDamaged = false;
}
