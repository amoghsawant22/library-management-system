package com.library.library_management_system.dto.request;

import lombok.*;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaginationDto {

    @Min(value = 0, message = "Page must be non-negative")
    @Builder.Default
    private Integer page = 0;

    @Min(value = 1, message = "Size must be at least 1")
    @Max(value = 100, message = "Size cannot exceed 100")
    @Builder.Default
    private Integer size = 10;

    private SortDto sort;

    // Helper method
    public int getOffset() {
        return page * size;
    }
}