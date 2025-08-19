package com.library.library_management_system.dto.request;

import lombok.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportRequest {

    @NotBlank(message = "Report type is required")
    @Pattern(regexp = "^(MOST_BORROWED_BOOKS|ACTIVE_MEMBERS|BOOK_AVAILABILITY|OVERDUE_BOOKS|GENRE_DISTRIBUTION|MONTHLY_TRENDS|CUSTOM)$",
            message = "Invalid report type")
    private String reportType;

    private LocalDate startDate;
    private LocalDate endDate;

    // Report-specific parameters
    private Integer limit = 10; // For top N reports

    // Genre filter for genre-specific reports
    private String genre;

    // User role filter
    private String userRole;

    // Include inactive records
    private Boolean includeInactive = false;

    // Custom parameters for flexible reporting
    private String customQuery;
    private String[] customParameters;

    // Output format
    @Pattern(regexp = "^(JSON|CSV|PDF)$", message = "Invalid format")
    @Builder.Default
    private String format = "JSON";
}