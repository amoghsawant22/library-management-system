package com.library.library_management_system.controller;

import com.library.library_management_system.dto.request.ReportRequest;
import com.library.library_management_system.dto.response.ApiResponse;
import com.library.library_management_system.dto.response.DashboardStatsResponse;
import com.library.library_management_system.dto.response.ReportResponse;
import com.library.library_management_system.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Reports and Analytics Controller
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reports & Analytics", description = "Library reports, analytics and dashboard endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class ReportController {

    private final ReportService reportService;

    // ============= Dashboard and Overview =============

    @Operation(summary = "Get dashboard statistics", description = "Get comprehensive dashboard statistics")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Dashboard statistics retrieved successfully",
                    content = @Content(schema = @Schema(implementation = DashboardStatsResponse.class)))
    })
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboardStats() {

        log.debug("Get dashboard statistics request");

        DashboardStatsResponse stats = reportService.getDashboardStats();

        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @Operation(summary = "Get executive summary", description = "Get executive summary report for specified period")
    @GetMapping("/executive-summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ReportResponse>> getExecutiveSummary(
            @Parameter(description = "Start date (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("Get executive summary request from {} to {}", startDate, endDate);

        ReportResponse report = reportService.getExecutiveSummary(startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success(report));
    }

    // ============= Standard Reports =============

    @Operation(summary = "Generate custom report", description = "Generate a custom report based on request parameters")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Report generated successfully",
                    content = @Content(schema = @Schema(implementation = ReportResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid report request parameters")
    })
    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ReportResponse>> generateReport(
            @Parameter(description = "Report generation parameters", required = true)
            @Valid @RequestBody ReportRequest request) {

        log.info("Generate custom report request: {}", request.getReportType());

        ReportResponse report = reportService.generateReport(request);

        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @Operation(summary = "Get most borrowed books report", description = "Get report of most frequently borrowed books")
    @GetMapping("/most-borrowed-books")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEMBER')")
    public ResponseEntity<ApiResponse<ReportResponse>> getMostBorrowedBooksReport(
            @Parameter(description = "Start date (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Number of books to include")
            @RequestParam(defaultValue = "10") int limit) {

        log.debug("Get most borrowed books report from {} to {} (limit: {})", startDate, endDate, limit);

        ReportResponse report = reportService.getMostBorrowedBooksReport(startDate, endDate, limit);

        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @Operation(summary = "Get active members report", description = "Get report of most active library members")
    @GetMapping("/active-members")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ReportResponse>> getActiveMembersReport(
            @Parameter(description = "Start date (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Number of members to include")
            @RequestParam(defaultValue = "10") int limit) {

        log.debug("Get active members report from {} to {} (limit: {})", startDate, endDate, limit);

        ReportResponse report = reportService.getActiveMembersReport(startDate, endDate, limit);

        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @Operation(summary = "Get book availability report", description = "Get comprehensive book availability report")
    @GetMapping("/book-availability")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ReportResponse>> getBookAvailabilityReport() {

        log.debug("Get book availability report request");

        ReportResponse report = reportService.getBookAvailabilityReport();

        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @Operation(summary = "Get overdue books report", description = "Get report of all overdue books and fines")
    @GetMapping("/overdue-books")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ReportResponse>> getOverdueBooksReport() {

        log.debug("Get overdue books report request");

        ReportResponse report = reportService.getOverdueBooksReport();

        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @Operation(summary = "Get genre distribution report", description = "Get report of book distribution by genre")
    @GetMapping("/genre-distribution")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEMBER')")
    public ResponseEntity<ApiResponse<ReportResponse>> getGenreDistributionReport() {

        log.debug("Get genre distribution report request");

        ReportResponse report = reportService.getGenreDistributionReport();

        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @Operation(summary = "Get monthly trends report", description = "Get monthly borrowing trends report")
    @GetMapping("/monthly-trends")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ReportResponse>> getMonthlyTrendsReport(
            @Parameter(description = "Start date (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.debug("Get monthly trends report from {} to {}", startDate, endDate);

        ReportResponse report = reportService.getMonthlyTrendsReport(startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @Operation(summary = "Get fine collection report", description = "Get report of fine collection and outstanding payments")
    @GetMapping("/fine-collection")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ReportResponse>> getFineCollectionReport(
            @Parameter(description = "Start date (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.debug("Get fine collection report from {} to {}", startDate, endDate);

        ReportResponse report = reportService.getFineCollectionReport(startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @Operation(summary = "Get user activity report", description = "Get report of user engagement and activity patterns")
    @GetMapping("/user-activity")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ReportResponse>> getUserActivityReport(
            @Parameter(description = "Start date (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.debug("Get user activity report from {} to {}", startDate, endDate);

        ReportResponse report = reportService.getUserActivityReport(startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @Operation(summary = "Get inventory health report", description = "Get report on inventory health and collection status")
    @GetMapping("/inventory-health")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ReportResponse>> getInventoryHealthReport() {

        log.debug("Get inventory health report request");

        ReportResponse report = reportService.getInventoryHealthReport();

        return ResponseEntity.ok(ApiResponse.success(report));
    }

    // ============= Raw Statistics and Data =============

    @Operation(summary = "Get library statistics", description = "Get raw library statistics data")
    @GetMapping("/stats/library")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Object[]>>> getLibraryStatistics() {

        log.debug("Get library statistics request");

        List<Object[]> stats = reportService.getLibraryStatistics();

        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @Operation(summary = "Get book circulation statistics", description = "Get book circulation statistics for specified period")
    @GetMapping("/stats/circulation")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Object[]>>> getBookCirculationStats(
            @Parameter(description = "Start date (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.debug("Get book circulation stats from {} to {}", startDate, endDate);

        List<Object[]> stats = reportService.getBookCirculationStats(startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @Operation(summary = "Get user engagement metrics", description = "Get user engagement metrics for specified period")
    @GetMapping("/stats/user-engagement")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Object[]>>> getUserEngagementMetrics(
            @Parameter(description = "Start date (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.debug("Get user engagement metrics from {} to {}", startDate, endDate);

        List<Object[]> metrics = reportService.getUserEngagementMetrics(startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success(metrics));
    }

    @Operation(summary = "Get popular books by genre", description = "Get popular books filtered by genre")
    @GetMapping("/stats/popular-books-by-genre")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEMBER')")
    public ResponseEntity<ApiResponse<List<Object[]>>> getPopularBooksByGenre(
            @Parameter(description = "Genre filter", required = true)
            @RequestParam String genre,
            @Parameter(description = "Number of books to return")
            @RequestParam(defaultValue = "10") int limit) {

        log.debug("Get popular books by genre: {} (limit: {})", genre, limit);

        List<Object[]> books = reportService.getPopularBooksByGenre(genre, limit);

        return ResponseEntity.ok(ApiResponse.success(books));
    }

    @Operation(summary = "Get library usage patterns", description = "Get library usage patterns by day of week")
    @GetMapping("/stats/usage-patterns")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Object[]>>> getLibraryUsagePatterns() {

        log.debug("Get library usage patterns request");

        List<Object[]> patterns = reportService.getLibraryUsagePatterns();

        return ResponseEntity.ok(ApiResponse.success(patterns));
    }

    // ============= Report Export =============

    @Operation(summary = "Export report to CSV", description = "Export report data in CSV format")
    @PostMapping("/export/csv")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportReportToCsv(
            @Parameter(description = "Report export parameters", required = true)
            @Valid @RequestBody ReportRequest request) {

        log.info("Export report to CSV: {}", request.getReportType());

        byte[] csvData = reportService.exportReportToCsv(request);

        String filename = String.format("%s_%s.csv",
                request.getReportType().toLowerCase(),
                LocalDate.now().toString());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvData);
    }

    @Operation(summary = "Export report to PDF", description = "Export report data in PDF format")
    @PostMapping("/export/pdf")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportReportToPdf(
            @Parameter(description = "Report export parameters", required = true)
            @Valid @RequestBody ReportRequest request) {

        log.info("Export report to PDF: {}", request.getReportType());

        byte[] pdfData = reportService.exportReportToPdf(request);

        String filename = String.format("%s_%s.pdf",
                request.getReportType().toLowerCase(),
                LocalDate.now().toString());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfData);
    }

    // ============= Cache Management =============

    @Operation(summary = "Clear report cache", description = "Clear all cached report data")
    @PostMapping("/cache/clear")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> clearReportCache() {

        log.info("Clear report cache request");

        reportService.clearReportCache();

        return ResponseEntity.ok(ApiResponse.success(null, "Report cache cleared successfully"));
    }

    @Operation(summary = "Get cached report", description = "Retrieve a cached report by key")
    @GetMapping("/cache/{reportKey}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ReportResponse>> getCachedReport(
            @Parameter(description = "Report cache key", required = true)
            @PathVariable String reportKey) {

        log.debug("Get cached report: {}", reportKey);

        ReportResponse report = reportService.getCachedReport(reportKey);

        if (report != null) {
            return ResponseEntity.ok(ApiResponse.success(report));
        } else {
            return ResponseEntity.ok(ApiResponse.success(null, "No cached report found for key: " + reportKey));
        }
    }

    // ============= Report Scheduling =============

    @Operation(summary = "Schedule report generation", description = "Schedule automatic report generation")
    @PostMapping("/schedule")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> scheduleReport(
            @Parameter(description = "Report scheduling parameters", required = true)
            @Valid @RequestBody ReportRequest request,
            @Parameter(description = "Cron expression for scheduling", required = true)
            @RequestParam String cronExpression) {

        log.info("Schedule report: {} with cron: {}", request.getReportType(), cronExpression);

        reportService.scheduleReport(request, cronExpression);

        return ResponseEntity.ok(ApiResponse.success(null,
                String.format("Report %s scheduled successfully", request.getReportType())));
    }
}