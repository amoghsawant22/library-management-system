package com.library.library_management_system.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Cache configuration for improved performance
 */
@Configuration
@EnableCaching
@EnableScheduling
@Slf4j
public class CacheConfig {

    // Cache names constants
    public static final String BOOKS_CACHE = "books";
    public static final String USERS_CACHE = "users";
    public static final String POPULAR_BOOKS_CACHE = "popularBooks";
    public static final String BOOK_STATS_CACHE = "bookStats";
    public static final String USER_STATS_CACHE = "userStats";
    public static final String DASHBOARD_CACHE = "dashboard";
    public static final String REPORTS_CACHE = "reports";

    @Bean
    public CacheManager cacheManager() {
        log.info("Initializing Cache Manager with caches: books, users, popularBooks, bookStats, userStats, dashboard, reports");

        return new ConcurrentMapCacheManager(
                BOOKS_CACHE,
                USERS_CACHE,
                POPULAR_BOOKS_CACHE,
                BOOK_STATS_CACHE,
                USER_STATS_CACHE,
                DASHBOARD_CACHE,
                REPORTS_CACHE
        );
    }

    /**
     * Clear all caches every hour to ensure fresh data
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    @CacheEvict(allEntries = true, value = {
            POPULAR_BOOKS_CACHE,
            BOOK_STATS_CACHE,
            USER_STATS_CACHE,
            DASHBOARD_CACHE,
            REPORTS_CACHE
    })
    public void clearStatsCache() {
        log.info("Cleared statistics and dashboard caches");
    }

    /**
     * Clear books cache every 30 minutes (books change frequently)
     */
    @Scheduled(fixedRate = 1800000) // 30 minutes
    @CacheEvict(allEntries = true, value = {BOOKS_CACHE})
    public void clearBooksCache() {
        log.info("Cleared books cache");
    }

    /**
     * Clear users cache every 2 hours (users change less frequently)
     */
    @Scheduled(fixedRate = 7200000) // 2 hours
    @CacheEvict(allEntries = true, value = {USERS_CACHE})
    public void clearUsersCache() {
        log.info("Cleared users cache");
    }

    /**
     * Clear reports cache daily at midnight
     */
    @Scheduled(cron = "0 0 0 * * *") // Daily at midnight
    @CacheEvict(allEntries = true, value = {REPORTS_CACHE})
    public void clearReportsCache() {
        log.info("Cleared reports cache - daily cleanup");
    }
}