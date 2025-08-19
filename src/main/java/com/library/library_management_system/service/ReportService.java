package com.library.library_management_system.service;

import com.library.library_management_system.dto.request.ReportRequest;
import com.library.library_management_system.dto.response.DashboardStatsResponse;
import com.library.library_management_system.dto.response.ReportResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * Report Service Interface
 */
public interface ReportService {

    /**
     * Generate dashboard statistics
     */
    DashboardStatsResponse getDashboardStats();

    /**
     * Generate custom report based on request
     */
    ReportResponse generateReport(ReportRequest request);

    /**
     * Generate most borrowed books report
     */
    ReportResponse getMostBorrowedBooksReport(LocalDate startDate, LocalDate endDate, int limit);

    /**
     * Generate active members report
     */
    ReportResponse getActiveMembersReport(LocalDate startDate, LocalDate endDate, int limit);

    /**
     * Generate book availability report
     */
    ReportResponse getBookAvailabilityReport();

    /**
     * Generate overdue books report
     */
    ReportResponse getOverdueBooksReport();

    /**
     * Generate genre distribution report
     */
    ReportResponse getGenreDistributionReport();

    /**
     * Generate monthly borrowing trends report
     */
    ReportResponse getMonthlyTrendsReport(LocalDate startDate, LocalDate endDate);

    /**
     * Generate fine collection report
     */
    ReportResponse getFineCollectionReport(LocalDate startDate, LocalDate endDate);

    /**
     * Generate user activity report
     */
    ReportResponse getUserActivityReport(LocalDate startDate, LocalDate endDate);

    /**
     * Generate inventory health report
     */
    ReportResponse getInventoryHealthReport();

    /**
     * Get real-time library statistics
     */
    List<Object[]> getLibraryStatistics();

    /**
     * Get book circulation statistics
     */
    List<Object[]> getBookCirculationStats(LocalDate startDate, LocalDate endDate);

    /**
     * Get user engagement metrics
     */
    List<Object[]> getUserEngagementMetrics(LocalDate startDate, LocalDate endDate);

    /**
     * Get popular books by genre
     */
    List<Object[]> getPopularBooksByGenre(String genre, int limit);

    /**
     * Get library usage patterns
     */
    List<Object[]> getLibraryUsagePatterns();

    /**
     * Generate executive summary report
     */
    ReportResponse getExecutiveSummary(LocalDate startDate, LocalDate endDate);

    /**
     * Export report data to CSV format
     */
    byte[] exportReportToCsv(ReportRequest request);

    /**
     * Export report data to PDF format
     */
    byte[] exportReportToPdf(ReportRequest request);

    /**
     * Schedule automatic report generation
     */
    void scheduleReport(ReportRequest request, String cronExpression);

    /**
     * Get cached report if available
     */
    ReportResponse getCachedReport(String reportKey);

    /**
     * Clear report cache
     */
    void clearReportCache();
}