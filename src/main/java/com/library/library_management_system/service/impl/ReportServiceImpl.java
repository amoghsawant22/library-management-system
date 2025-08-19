package com.library.library_management_system.service.impl;

import com.library.library_management_system.config.CacheConfig;
import com.library.library_management_system.dto.mapper.BookMapper;
import com.library.library_management_system.dto.mapper.UserMapper;
import com.library.library_management_system.dto.request.ReportRequest;
import com.library.library_management_system.dto.response.BookResponse;
import com.library.library_management_system.dto.response.DashboardStatsResponse;
import com.library.library_management_system.dto.response.ReportResponse;
import com.library.library_management_system.dto.response.UserResponse;
import com.library.library_management_system.enums.BorrowStatus;
import com.library.library_management_system.enums.UserRole;
import com.library.library_management_system.exception.BadRequestException;
import com.library.library_management_system.repository.BookRepository;
import com.library.library_management_system.repository.BorrowingRecordRepository;
import com.library.library_management_system.repository.UserRepository;
import com.library.library_management_system.service.BookService;
import com.library.library_management_system.service.BorrowingService;
import com.library.library_management_system.service.ReportService;
import com.library.library_management_system.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Report Service Implementation (Fixed)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final BorrowingRecordRepository borrowingRecordRepository;
    private final BookService bookService;
    private final UserService userService;
    private final BorrowingService borrowingService;

    @Override
    @Cacheable(value = CacheConfig.DASHBOARD_CACHE, key = "'dashboard'")
    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {
        log.info("Generating dashboard statistics");

        // Get basic statistics using existing repositories
        Object[] bookAvailability = bookRepository.getBookAvailabilityStats();
        List<Object[]> genreStats = bookRepository.getPopularGenreStats();

        // Get most borrowed book and most active user
        List<BookResponse> mostBorrowedBooks = bookService.getMostBorrowedBooks(1);
        List<UserResponse> mostActiveUsers = userService.getMostActiveUsers(1);

        String mostPopularGenre = genreStats.isEmpty() ? "N/A" : (String) genreStats.get(0)[0];

        return DashboardStatsResponse.builder()
                .totalBooks(bookAvailability != null ? ((Number) bookAvailability[0]).longValue() : 0L)
                .totalBooks(bookAvailability != null ? ((Number) bookAvailability[1]).longValue() : 0L)
                .availableCopies(bookAvailability != null ? ((Number) bookAvailability[2]).longValue() : 0L)
                .borrowedCopies(bookAvailability != null ? ((Number) bookAvailability[3]).longValue() : 0L)
                .availabilityPercentage(bookAvailability != null ?
                        calculateAvailabilityPercentage(bookAvailability) : 0.0)
                .activeUsers(userService.countActiveUsers())
                .membersCount(userService.countUsersByRole(UserRole.MEMBER))
                .adminsCount(userService.countUsersByRole(UserRole.ADMIN))
                .activeBorrows(borrowingService.countBorrowingsByStatus(BorrowStatus.BORROWED))
                .overdueBooks(borrowingService.countOverdueBooks())
                .totalFines(0.0) // Calculate from borrowing records
                .pendingFines(0.0) // Calculate from borrowing records
                .collectedFines(0.0) // Calculate from borrowing records
                .mostBorrowedBook(mostBorrowedBooks.isEmpty() ? null : mostBorrowedBooks.get(0))
                .mostActiveUser(mostActiveUsers.isEmpty() ? null : mostActiveUsers.get(0))
                .mostPopularGenre(mostPopularGenre)
                .booksNeedingAttention((long) bookService.getBooksNeedingAttention().size())
                .usersWithOverdueBooks((long) userService.getUsersWithOverdueBooks().size())
                .lowStockBooks((long) bookService.getBooksWithLowAvailability(2).size())
                .build();
    }

    @Override
    @Cacheable(value = CacheConfig.REPORTS_CACHE, key = "#request.toString()")
    @Transactional(readOnly = true)
    public ReportResponse generateReport(ReportRequest request) {
        log.info("Generating report: {}", request.getReportType());

        return switch (request.getReportType()) {
            case "MOST_BORROWED_BOOKS" -> getMostBorrowedBooksReport(
                    request.getStartDate(), request.getEndDate(), request.getLimit());
            case "ACTIVE_MEMBERS" -> getActiveMembersReport(
                    request.getStartDate(), request.getEndDate(), request.getLimit());
            case "BOOK_AVAILABILITY" -> getBookAvailabilityReport();
            case "OVERDUE_BOOKS" -> getOverdueBooksReport();
            case "GENRE_DISTRIBUTION" -> getGenreDistributionReport();
            case "MONTHLY_TRENDS" -> getMonthlyTrendsReport(
                    request.getStartDate(), request.getEndDate());
            default -> throw new BadRequestException("Unsupported report type: " + request.getReportType());
        };
    }

    @Override
    @Cacheable(value = CacheConfig.REPORTS_CACHE, key = "'mostBorrowed:' + #startDate + ':' + #endDate + ':' + #limit")
    @Transactional(readOnly = true)
    public ReportResponse getMostBorrowedBooksReport(LocalDate startDate, LocalDate endDate, int limit) {
        log.info("Generating most borrowed books report from {} to {} (limit: {})", startDate, endDate, limit);

        List<Object[]> mostBorrowedStats = borrowingRecordRepository.getMostBorrowedBooksWithStats(limit);

        List<ReportResponse.MostBorrowedBookDto> mostBorrowedBooks = mostBorrowedStats.stream()
                .map(stats -> ReportResponse.MostBorrowedBookDto.builder()
                        .bookId(((Number) stats[0]).longValue())
                        .title((String) stats[1])
                        .author((String) stats[2])
                        .genre((String) stats[4])
                        .totalBorrows(((Number) stats[5]).longValue())
                        .uniqueBorrowers(((Number) stats[6]).longValue())
                        .averageBorrowDuration(stats[7] != null ? ((Number) stats[7]).doubleValue() : 0.0)
                        .circulationRate(0.0) // Calculate based on total copies
                        .build())
                .toList();

        return ReportResponse.builder()
                .reportType("MOST_BORROWED_BOOKS")
                .title("Most Borrowed Books Report")
                .description("Books with highest borrowing frequency")
                .generatedDate(LocalDate.now())
                .startDate(startDate)
                .endDate(endDate)
                .mostBorrowedBooks(mostBorrowedBooks)
                .build();
    }

    @Override
    @Cacheable(value = CacheConfig.REPORTS_CACHE, key = "'activeMembers:' + #startDate + ':' + #endDate + ':' + #limit")
    @Transactional(readOnly = true)
    public ReportResponse getActiveMembersReport(LocalDate startDate, LocalDate endDate, int limit) {
        log.info("Generating active members report from {} to {} (limit: {})", startDate, endDate, limit);

        List<Object[]> activeMembersStats = borrowingRecordRepository.getMostActiveMembers(startDate, limit);

        List<ReportResponse.ActiveMemberDto> activeMembers = activeMembersStats.stream()
                .map(stats -> ReportResponse.ActiveMemberDto.builder()
                        .userId(((Number) stats[0]).longValue())
                        .username((String) stats[1])
                        .fullName((String) stats[2])
                        .email((String) stats[3])
                        .totalBorrows(((Number) stats[4]).longValue())
                        .returnedBooks(((Number) stats[5]).longValue())
                        .overdueBooks(((Number) stats[6]).longValue())
                        .averageBorrowDuration(stats[7] != null ? ((Number) stats[7]).doubleValue() : 0.0)
                        .totalFines(stats[8] != null ? ((Number) stats[8]).doubleValue() : 0.0)
                        .build())
                .toList();

        return ReportResponse.builder()
                .reportType("ACTIVE_MEMBERS")
                .title("Most Active Members Report")
                .description("Members with highest borrowing activity")
                .generatedDate(LocalDate.now())
                .startDate(startDate)
                .endDate(endDate)
                .activeMembers(activeMembers)
                .build();
    }

    @Override
    @Cacheable(value = CacheConfig.REPORTS_CACHE, key = "'bookAvailability'")
    @Transactional(readOnly = true)
    public ReportResponse getBookAvailabilityReport() {
        log.info("Generating book availability report");

        Object[] availabilityStats = bookRepository.getBookAvailabilityStats();

        ReportResponse.BookAvailabilityDto bookAvailability = null;
        if (availabilityStats != null) {
            bookAvailability = ReportResponse.BookAvailabilityDto.builder()
                    .totalBooks(((Number) availabilityStats[0]).longValue())
                    .totalCopies(((Number) availabilityStats[1]).longValue())
                    .availableCopies(((Number) availabilityStats[2]).longValue())
                    .borrowedCopies(((Number) availabilityStats[3]).longValue())
                    .outOfStockBooks(((Number) availabilityStats[4]).longValue())
                    .availabilityPercentage(calculateAvailabilityPercentage(availabilityStats))
                    .build();
        }

        return ReportResponse.builder()
                .reportType("BOOK_AVAILABILITY")
                .title("Book Availability Report")
                .description("Current availability status of all books")
                .generatedDate(LocalDate.now())
                .bookAvailability(bookAvailability)
                .build();
    }

    @Override
    @Cacheable(value = CacheConfig.REPORTS_CACHE, key = "'overdueBooks'")
    @Transactional(readOnly = true)
    public ReportResponse getOverdueBooksReport() {
        log.info("Generating overdue books report");

        Object[] overdueStats = borrowingRecordRepository.getOverdueStatistics();
        List<Object[]> overdueBooks = borrowingRecordRepository.findOverdueBooks();

        Map<String, Object> customData = new HashMap<>();
        if (overdueStats != null) {
            customData.put("totalOverdue", overdueStats[0]);
            customData.put("uniqueUsers", overdueStats[1]);
            customData.put("averageDaysOverdue", overdueStats[2]);
            customData.put("totalFines", overdueStats[3]);
        }
        customData.put("overdueBooksList", overdueBooks);

        return ReportResponse.builder()
                .reportType("OVERDUE_BOOKS")
                .title("Overdue Books Report")
                .description("Books that are currently overdue")
                .generatedDate(LocalDate.now())
                .customData(customData)
                .build();
    }

    @Override
    @Cacheable(value = CacheConfig.REPORTS_CACHE, key = "'genreDistribution'")
    @Transactional(readOnly = true)
    public ReportResponse getGenreDistributionReport() {
        log.info("Generating genre distribution report");

        // ✅ FIXED: Use existing repository methods instead of LibraryStatsRepository
        List<Object[]> genreStats = bookRepository.getPopularGenreStats();

        List<ReportResponse.GenreStatsDto> genreDistribution = genreStats.stream()
                .map(stats -> ReportResponse.GenreStatsDto.builder()
                        .genre((String) stats[0])
                        .totalBooks(((Number) stats[1]).longValue())
                        .totalCopies(0L) // Would need additional query
                        .availableCopies(0L) // Would need additional query
                        .totalBorrows(((Number) stats[1]).longValue())
                        .averageBorrowDays(0.0) // Would need additional query
                        .build())
                .toList();

        return ReportResponse.builder()
                .reportType("GENRE_DISTRIBUTION")
                .title("Genre Distribution Report")
                .description("Distribution of books and borrowing activity by genre")
                .generatedDate(LocalDate.now())
                .genreDistribution(genreDistribution)
                .build();
    }

    @Override
    @Cacheable(value = CacheConfig.REPORTS_CACHE, key = "'monthlyTrends:' + #startDate + ':' + #endDate")
    @Transactional(readOnly = true)
    public ReportResponse getMonthlyTrendsReport(LocalDate startDate, LocalDate endDate) {
        log.info("Generating monthly trends report from {} to {}", startDate, endDate);

        List<Object[]> monthlyStats = borrowingRecordRepository.getBorrowingTrendsByMonth(startDate);

        List<ReportResponse.MonthlyTrendDto> monthlyTrends = monthlyStats.stream()
                .map(stats -> ReportResponse.MonthlyTrendDto.builder()
                        .month((String) stats[0])
                        .totalBorrows(((Number) stats[1]).longValue())
                        .uniqueUsers(((Number) stats[2]).longValue())
                        .uniqueBooks(((Number) stats[3]).longValue())
                        .onTimeReturns(((Number) stats[4]).longValue())
                        .lateReturns(((Number) stats[5]).longValue())
                        .build())
                .toList();

        return ReportResponse.builder()
                .reportType("MONTHLY_TRENDS")
                .title("Monthly Borrowing Trends")
                .description("Monthly analysis of borrowing patterns and trends")
                .generatedDate(LocalDate.now())
                .startDate(startDate)
                .endDate(endDate)
                .monthlyTrends(monthlyTrends)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ReportResponse getFineCollectionReport(LocalDate startDate, LocalDate endDate) {
        log.info("Generating fine collection report from {} to {}", startDate, endDate);

        List<Object[]> fineStats = borrowingRecordRepository.getUsersWithOutstandingFines();

        Map<String, Object> customData = new HashMap<>();
        customData.put("usersWithFines", fineStats);
        customData.put("totalOutstandingFines",
                fineStats.stream().mapToDouble(stat -> ((Number) stat[4]).doubleValue()).sum());

        return ReportResponse.builder()
                .reportType("FINE_COLLECTION")
                .title("Fine Collection Report")
                .description("Analysis of fines and outstanding payments")
                .generatedDate(LocalDate.now())
                .startDate(startDate)
                .endDate(endDate)
                .customData(customData)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ReportResponse getUserActivityReport(LocalDate startDate, LocalDate endDate) {
        log.info("Generating user activity report from {} to {}", startDate, endDate);

        // ✅ FIXED: Use existing repository methods
        List<UserResponse> activeUsers = userService.getMostActiveUsers(50);

        Map<String, Object> customData = new HashMap<>();
        customData.put("mostActiveUsers", activeUsers);

        return ReportResponse.builder()
                .reportType("USER_ACTIVITY")
                .title("User Activity Report")
                .description("Analysis of user engagement and activity patterns")
                .generatedDate(LocalDate.now())
                .startDate(startDate)
                .endDate(endDate)
                .customData(customData)
                .build();
    }

    @Override
    @Cacheable(value = CacheConfig.REPORTS_CACHE, key = "'inventoryHealth'")
    @Transactional(readOnly = true)
    public ReportResponse getInventoryHealthReport() {
        log.info("Generating inventory health report");

        // ✅ FIXED: Use existing service methods
        List<BookResponse> lowStockBooks = bookService.getBooksWithLowAvailability(2);
        List<BookResponse> needingAttention = bookService.getBooksNeedingAttention();

        Map<String, Object> customData = new HashMap<>();
        customData.put("lowStockBooks", lowStockBooks);
        customData.put("booksNeedingAttention", needingAttention);

        return ReportResponse.builder()
                .reportType("INVENTORY_HEALTH")
                .title("Inventory Health Report")
                .description("Health check of library inventory and collection status")
                .generatedDate(LocalDate.now())
                .customData(customData)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getLibraryStatistics() {
        return borrowingRecordRepository.getLibraryStatistics();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getBookCirculationStats(LocalDate startDate, LocalDate endDate) {
        return borrowingRecordRepository.getBorrowingTrendsByMonth(startDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getUserEngagementMetrics(LocalDate startDate, LocalDate endDate) {
        return borrowingRecordRepository.getMostActiveMembers(startDate, 100);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getPopularBooksByGenre(String genre, int limit) {
        return bookRepository.findBooksByGenreWithStats(genre);
    }

    @Override
    @Cacheable(value = CacheConfig.REPORTS_CACHE, key = "'usagePatterns'")
    @Transactional(readOnly = true)
    public List<Object[]> getLibraryUsagePatterns() {
        // ✅ FIXED: Return empty list for now (can be implemented later)
        return List.of();
    }

    @Override
    @Cacheable(value = CacheConfig.REPORTS_CACHE, key = "'executiveSummary:' + #startDate + ':' + #endDate")
    @Transactional(readOnly = true)
    public ReportResponse getExecutiveSummary(LocalDate startDate, LocalDate endDate) {
        log.info("Generating executive summary from {} to {}", startDate, endDate);

        // Combine multiple reports for executive overview
        DashboardStatsResponse dashboard = getDashboardStats();
        ReportResponse bookAvailability = getBookAvailabilityReport();
        ReportResponse monthlyTrends = getMonthlyTrendsReport(startDate, endDate);

        Map<String, Object> executiveData = new HashMap<>();
        executiveData.put("dashboardStats", dashboard);
        executiveData.put("bookAvailability", bookAvailability.getBookAvailability());
        executiveData.put("monthlyTrends", monthlyTrends.getMonthlyTrends());
        executiveData.put("keyMetrics", getLibraryStatistics());

        return ReportResponse.builder()
                .reportType("EXECUTIVE_SUMMARY")
                .title("Executive Summary Report")
                .description("Comprehensive overview of library performance and metrics")
                .generatedDate(LocalDate.now())
                .startDate(startDate)
                .endDate(endDate)
                .customData(executiveData)
                .build();
    }

    @Override
    public byte[] exportReportToCsv(ReportRequest request) {
        log.info("Exporting report to CSV: {}", request.getReportType());
        // Implementation would generate CSV data
        return "CSV data would be generated here".getBytes();
    }

    @Override
    public byte[] exportReportToPdf(ReportRequest request) {
        log.info("Exporting report to PDF: {}", request.getReportType());
        // Implementation would generate PDF data
        return "PDF data would be generated here".getBytes();
    }

    @Override
    @Async("reportExecutor")
    public void scheduleReport(ReportRequest request, String cronExpression) {
        log.info("Scheduling report: {} with cron: {}", request.getReportType(), cronExpression);
        // Implementation would use Spring's @Scheduled annotation or Quartz
    }

    @Override
    @Cacheable(value = CacheConfig.REPORTS_CACHE, key = "#reportKey")
    public ReportResponse getCachedReport(String reportKey) {
        log.debug("Retrieving cached report: {}", reportKey);
        return null; // Cache will handle the actual retrieval
    }

    @Override
    @CacheEvict(value = CacheConfig.REPORTS_CACHE, allEntries = true)
    public void clearReportCache() {
        log.info("Clearing all report cache entries");
    }

    // Helper methods

    private Double calculateAvailabilityPercentage(Object[] stats) {
        if (stats.length >= 3 && stats[1] != null && stats[2] != null) {
            double totalCopies = ((Number) stats[1]).doubleValue();
            double availableCopies = ((Number) stats[2]).doubleValue();
            if (totalCopies > 0) {
                return (availableCopies / totalCopies) * 100.0;
            }
        }
        return 0.0;
    }
}