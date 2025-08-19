package com.library.library_management_system.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportResponse {

    private String reportType;
    private String title;
    private String description;
    private LocalDate generatedDate;
    private LocalDate startDate;
    private LocalDate endDate;

    // Most Borrowed Books Report
    private List<MostBorrowedBookDto> mostBorrowedBooks;

    // Active Members Report
    private List<ActiveMemberDto> activeMembers;

    // Book Availability Report
    private BookAvailabilityDto bookAvailability;

    // Genre Distribution
    private List<GenreStatsDto> genreDistribution;

    // Monthly Trends
    private List<MonthlyTrendDto> monthlyTrends;

    // Custom data for flexible reporting
    private Map<String, Object> customData;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MostBorrowedBookDto {
        private Long bookId;
        private String title;
        private String author;
        private String genre;
        private Long totalBorrows;
        private Long uniqueBorrowers;
        private Double averageBorrowDuration;
        private Double circulationRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActiveMemberDto {
        private Long userId;
        private String username;
        private String fullName;
        private String email;
        private Long totalBorrows;
        private Long returnedBooks;
        private Long overdueBooks;
        private Double averageBorrowDuration;
        private Double totalFines;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookAvailabilityDto {
        private Long totalBooks;
        private Long totalCopies;
        private Long availableCopies;
        private Long borrowedCopies;
        private Long outOfStockBooks;
        private Double availabilityPercentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenreStatsDto {
        private String genre;
        private Long totalBooks;
        private Long totalCopies;
        private Long availableCopies;
        private Long totalBorrows;
        private Double averageBorrowDays;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyTrendDto {
        private String month;
        private Long totalBorrows;
        private Long uniqueUsers;
        private Long uniqueBooks;
        private Long onTimeReturns;
        private Long lateReturns;
    }
}
