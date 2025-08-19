package com.library.library_management_system.dto.request;

import lombok.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SortDto {

    @NotBlank(message = "Sort field is required")
    private String field;

    @Pattern(regexp = "^(asc|desc)$", message = "Direction must be 'asc' or 'desc'")
    @Builder.Default
    private String direction = "asc";

    // For multi-field sorting
    private Integer priority = 0;
}
