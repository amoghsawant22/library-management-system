package com.library.library_management_system.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsResponse {

    // Book statistics
    private Long totalBooks;
    private Long availableBooks;
    private Long borrowedBooks;
    private Long overdueBooks;
    private Long borrowedCopies;
    private Long availableCopies;
    private Long totalCopies;
    private Double availabilityPercentage;

    // User statistics
    private Long totalUsers;
    private Long activeUsers;
    private Long membersCount;
    private Long adminsCount;

    // Borrowing statistics
    private Long activeBorrows;
    private Long totalBorrows;
    private Long todaysBorrows;
    private Long todaysReturns;

    // Financial statistics
    private Double totalFines;
    private Double pendingFines;
    private Double collectedFines;

    // Popular items
    private BookResponse mostBorrowedBook;
    private UserResponse mostActiveUser;
    private String mostPopularGenre;

    // Alerts
    private Long booksNeedingAttention;
    private Long usersWithOverdueBooks;
    private Long lowStockBooks;
}
