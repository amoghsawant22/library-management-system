package com.library.library_management_system.dto.request;

import com.library.library_management_system.enums.UserRole;
import lombok.*;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSearchRequest {

    private String searchTerm; // Search in username, fullName, email
    private String username;
    private String fullName;
    private String email;
    private UserRole role;
    private Boolean isActive = true;

    // Date filters
    private LocalDate membershipStartDate;
    private LocalDate membershipEndDate;
    private LocalDate birthDateStart;
    private LocalDate birthDateEnd;

    // Statistics filters
    private Integer minBorrowedBooks;
    private Integer maxBorrowedBooks;
    private Double minFines;
    private Double maxFines;

    // Sorting options
    @Pattern(regexp = "^(username|fullName|email|role|membershipDate|createdAt|totalBorrows)$",
            message = "Invalid sort field")
    @Builder.Default
    private String sortBy = "fullName";

    @Pattern(regexp = "^(asc|desc)$", message = "Sort direction must be 'asc' or 'desc'")
    @Builder.Default
    private String sortDirection = "asc";

    // Pagination
    @Min(value = 0, message = "Page must be non-negative")
    @Builder.Default
    private Integer page = 0;

    @Min(value = 1, message = "Size must be at least 1")
    @Builder.Default
    private Integer size = 10;
}