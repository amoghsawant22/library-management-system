package com.library.library_management_system.service;

import com.library.library_management_system.dto.request.BorrowBookRequest;
import com.library.library_management_system.dto.request.BorrowingSearchRequest;
import com.library.library_management_system.dto.request.ReturnBookRequest;
import com.library.library_management_system.dto.response.BorrowingHistoryResponse;
import com.library.library_management_system.dto.response.PagedResponse;
import com.library.library_management_system.enums.BorrowStatus;

import java.time.LocalDate;
import java.util.List;

/**
 * Borrowing Service Interface
 */
public interface BorrowingService {

    /**
     * Borrow a book
     */
    BorrowingHistoryResponse borrowBook(BorrowBookRequest request);

    /**
     * Return a borrowed book
     */
    BorrowingHistoryResponse returnBook(ReturnBookRequest request);

    /**
     * Renew a borrowed book
     */
    BorrowingHistoryResponse renewBook(Long borrowingRecordId, int additionalDays);

    /**
     * Mark book as lost
     */
    BorrowingHistoryResponse markBookAsLost(Long borrowingRecordId);

    /**
     * Get borrowing record by ID
     */
    BorrowingHistoryResponse getBorrowingRecordById(Long id);

    /**
     * Get user's borrowing history
     */
    PagedResponse<BorrowingHistoryResponse> getUserBorrowingHistory(Long userId, int page, int size);

    /**
     * Get current user's borrowing history
     */
    PagedResponse<BorrowingHistoryResponse> getCurrentUserBorrowingHistory(int page, int size);

    /**
     * Get user's currently borrowed books
     */
    List<BorrowingHistoryResponse> getUserCurrentBorrowedBooks(Long userId);

    /**
     * Get current user's borrowed books
     */
    List<BorrowingHistoryResponse> getCurrentUserBorrowedBooks();

    /**
     * Get all borrowing records with pagination
     */
    PagedResponse<BorrowingHistoryResponse> getAllBorrowingRecords(int page, int size, String sortBy, String sortDirection);

    /**
     * Search borrowing records with filters
     */
    PagedResponse<BorrowingHistoryResponse> searchBorrowingRecords(BorrowingSearchRequest request);

    /**
     * Get overdue books
     */
    List<BorrowingHistoryResponse> getOverdueBooks();

    /**
     * Get borrowing records by status
     */
    List<BorrowingHistoryResponse> getBorrowingRecordsByStatus(BorrowStatus status);

    /**
     * Get borrowing records for a book
     */
    PagedResponse<BorrowingHistoryResponse> getBookBorrowingHistory(Long bookId, int page, int size);

    /**
     * Update overdue records (mark as overdue)
     */
    int updateOverdueRecords();

    /**
     * Calculate and update fines for overdue books
     */
    int updateOverdueFines(double finePerDay);

    /**
     * Get users with outstanding fines
     */
    List<Object[]> getUsersWithOutstandingFines();

    /**
     * Get borrowing statistics for dashboard
     */
    List<Object[]> getLibraryStatistics();

    /**
     * Get overdue statistics
     */
    Object[] getOverdueStatistics();

    /**
     * Get borrowing trends by month
     */
    List<Object[]> getBorrowingTrendsByMonth(LocalDate startDate);

    /**
     * Check if user can borrow book
     */
    boolean canUserBorrowBook(Long userId, Long bookId);

    /**
     * Check if book can be returned
     */
    boolean canReturnBook(Long borrowingRecordId);

    /**
     * Check if book can be renewed
     */
    boolean canRenewBook(Long borrowingRecordId);

    /**
     * Get user's borrowing capacity info
     */
    Object[] getUserBorrowingCapacity(Long userId);

    /**
     * Count active borrowings for user
     */
    long countActiveUserBorrowings(Long userId);

    /**
     * Count overdue books
     */
    long countOverdueBooks();

    /**
     * Count books by status
     */
    long countBorrowingsByStatus(BorrowStatus status);
}