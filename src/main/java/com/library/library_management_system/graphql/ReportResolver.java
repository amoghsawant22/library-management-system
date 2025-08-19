package com.library.library_management_system.graphql;

import com.library.library_management_system.dto.response.DashboardStatsResponse;
import com.library.library_management_system.service.BookService;
import com.library.library_management_system.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ReportResolver {

    private final ReportService reportService;
    private final BookService bookService; // ✅ Added BookService for book stats

    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public DashboardStatsResponse dashboardStats() {
        log.debug("GraphQL: Get dashboard statistics");
        return reportService.getDashboardStats();
    }

    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> bookAvailabilityStats() {
        log.debug("GraphQL: Get book availability statistics");

        // ✅ FIXED: Use BookService instead of ReportService
        Object[] stats = bookService.getBookAvailabilityStats();

        if (stats != null && stats.length >= 5) {
            return Map.of(
                    "totalBooks", stats[0],
                    "totalCopies", stats[1],
                    "availableCopies", stats[2],
                    "borrowedCopies", stats[3],
                    "outOfStockBooks", stats[4]
            );
        }

        return Map.of();
    }

    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Object[]> libraryStatistics() {
        log.debug("GraphQL: Get library statistics");
        return reportService.getLibraryStatistics();
    }

    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Object[]> popularGenreStats() {
        log.debug("GraphQL: Get popular genre statistics");
        // ✅ FIXED: Use BookService for genre stats
        return bookService.getPopularGenreStats();
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Boolean clearReportCache() {
        log.info("GraphQL: Clear report cache");
        reportService.clearReportCache();
        return true;
    }
}