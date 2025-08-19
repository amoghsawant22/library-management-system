package com.library.library_management_system.dto.response;

import com.library.library_management_system.enums.UserRole;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String username;
    private String fullName;
    private String email;
    private UserRole role;
    private String phoneNumber;
    private String address;
    private LocalDate dateOfBirth;
    private LocalDate membershipDate;
    private Boolean isActive;
    private Integer maxBooksAllowed;

    // Statistics
    private Long totalBorrowedBooks;
    private Long currentlyBorrowedBooks;
    private Long overdueBooks;
    private Double totalFines;

    // Audit fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}