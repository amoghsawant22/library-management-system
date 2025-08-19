package com.library.library_management_system.repository;

import com.library.library_management_system.entity.BorrowingRecord;
import com.library.library_management_system.enums.BorrowStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for BorrowingRecord entity with comprehensive native SQL queries
 */
@Repository
public interface BorrowingRecordRepository extends JpaRepository<BorrowingRecord, Long> {

    // ============= Basic CRUD Operations =============

    List<BorrowingRecord> findByUserId(Long userId);

    List<BorrowingRecord> findByBookId(Long bookId);

    List<BorrowingRecord> findByStatus(BorrowStatus status);

    // ============= User Borrowing History Queries =============

    /**
     * Get user's borrowing history with pagination using native SQL
     */
    @Query(value = """
        SELECT br.*, b.title as book_title, b.author as book_author
        FROM borrowing_records br
        INNER JOIN books b ON br.book_id = b.id
        WHERE br.user_id = :userId
        ORDER BY br.borrow_date DESC
        """, nativeQuery = true)
    Page<Object[]> findUserBorrowingHistory(@Param("userId") Long userId, Pageable pageable);

    /**
     * Get user's currently borrowed books using native SQL
     */
    @Query(value = """
        SELECT br.*, b.title, b.author, b.isbn
        FROM borrowing_records br
        INNER JOIN books b ON br.book_id = b.id
        WHERE br.user_id = :userId 
        AND br.status = 'BORROWED'
        ORDER BY br.due_date ASC
        """, nativeQuery = true)
    List<Object[]> findUserCurrentBorrowedBooks(@Param("userId") Long userId);

    /**
     * Check if user can borrow more books using native SQL
     */
    @Query(value = """
        SELECT 
            u.max_books_allowed,
            COUNT(br.id) as current_borrowed
        FROM users u
        LEFT JOIN borrowing_records br ON u.id = br.user_id 
            AND br.status = 'BORROWED'
        WHERE u.id = :userId
        GROUP BY u.id, u.max_books_allowed
        """, nativeQuery = true)
    Object[] getUserBorrowingCapacity(@Param("userId") Long userId);

    // ============= Overdue Management Queries =============

    /**
     * Find all overdue books using native SQL
     */
    @Query(value = """
        SELECT 
            br.*,
            u.username,
            u.full_name,
            u.email,
            b.title,
            b.author,
            DATEDIFF(CURRENT_DATE, br.due_date) as days_overdue
        FROM borrowing_records br
        INNER JOIN users u ON br.user_id = u.id
        INNER JOIN books b ON br.book_id = b.id
        WHERE br.status IN ('BORROWED', 'OVERDUE')
        AND br.due_date < CURRENT_DATE
        ORDER BY days_overdue DESC
        """, nativeQuery = true)
    List<Object[]> findOverdueBooks();

    /**
     * Update overdue records status using native SQL
     */
    @Modifying
    @Query(value = """
        UPDATE borrowing_records SET 
            status = 'OVERDUE',
            updated_at = CURRENT_TIMESTAMP
        WHERE status = 'BORROWED' 
        AND due_date < CURRENT_DATE
        """, nativeQuery = true)
    int markOverdueRecords();

    /**
     * Get overdue statistics using native SQL
     */
    @Query(value = """
        SELECT 
            COUNT(*) as total_overdue,
            COUNT(DISTINCT br.user_id) as unique_users,
            AVG(DATEDIFF(CURRENT_DATE, br.due_date)) as avg_days_overdue,
            SUM(br.fine_amount) as total_fines
        FROM borrowing_records br
        WHERE br.status = 'OVERDUE'
        """, nativeQuery = true)
    Object[] getOverdueStatistics();

    // ============= Popular Books and Analytics =============

    /**
     * Get most borrowed books with statistics using native SQL
     */
    @Query(value = """
        SELECT 
            b.id,
            b.title,
            b.author,
            b.isbn,
            b.genre,
            COUNT(br.id) as total_borrows,
            COUNT(DISTINCT br.user_id) as unique_borrowers,
            AVG(CASE 
                WHEN br.return_date IS NOT NULL 
                THEN DATEDIFF(br.return_date, br.borrow_date)
                ELSE NULL 
            END) as avg_borrow_duration
        FROM books b
        INNER JOIN borrowing_records br ON b.id = br.book_id
        WHERE b.is_active = true
        GROUP BY b.id
        ORDER BY total_borrows DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> getMostBorrowedBooksWithStats(@Param("limit") int limit);

    /**
     * Get borrowing trends by month using native SQL
     */
    @Query(value = """
        SELECT 
            DATE_FORMAT(br.borrow_date, '%Y-%m') as month,
            COUNT(*) as total_borrows,
            COUNT(DISTINCT br.user_id) as unique_users,
            COUNT(DISTINCT br.book_id) as unique_books
        FROM borrowing_records br
        WHERE br.borrow_date >= :startDate
        GROUP BY DATE_FORMAT(br.borrow_date, '%Y-%m')
        ORDER BY month DESC
        """, nativeQuery = true)
    List<Object[]> getBorrowingTrendsByMonth(@Param("startDate") LocalDate startDate);

    // ============= Active Members Analytics =============

    /**
     * Find most active members using native SQL
     */
    @Query(value = """
        SELECT 
            u.id,
            u.username,
            u.full_name,
            u.email,
            COUNT(br.id) as total_borrows,
            COUNT(CASE WHEN br.status = 'RETURNED' THEN 1 END) as returned_books,
            COUNT(CASE WHEN br.status = 'OVERDUE' THEN 1 END) as overdue_books,
            AVG(CASE 
                WHEN br.return_date IS NOT NULL 
                THEN DATEDIFF(br.return_date, br.borrow_date)
                ELSE NULL 
            END) as avg_borrow_duration,
            SUM(br.fine_amount) as total_fines
        FROM users u
        INNER JOIN borrowing_records br ON u.id = br.user_id
        WHERE u.is_active = true
        AND br.borrow_date >= :startDate
        GROUP BY u.id
        ORDER BY total_borrows DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> getMostActiveMembers(@Param("startDate") LocalDate startDate, @Param("limit") int limit);

    // ============= Fine Management =============

    /**
     * Calculate and update fines for overdue books using native SQL
     */
    @Modifying
    @Query(value = """
        UPDATE borrowing_records SET 
            fine_amount = DATEDIFF(CURRENT_DATE, due_date) * :finePerDay,
            updated_at = CURRENT_TIMESTAMP
        WHERE status = 'OVERDUE' 
        AND due_date < CURRENT_DATE
        """, nativeQuery = true)
    int updateOverdueFines(@Param("finePerDay") double finePerDay);

    /**
     * Get users with outstanding fines using native SQL
     */
    @Query(value = """
        SELECT 
            u.id,
            u.username,
            u.full_name,
            u.email,
            SUM(br.fine_amount) as total_fines,
            COUNT(br.id) as overdue_books
        FROM users u
        INNER JOIN borrowing_records br ON u.id = br.user_id
        WHERE br.fine_amount > 0
        AND br.status IN ('OVERDUE', 'RETURNED')
        GROUP BY u.id
        HAVING total_fines > 0
        ORDER BY total_fines DESC
        """, nativeQuery = true)
    List<Object[]> getUsersWithOutstandingFines();

    // ============= Book Return Operations =============

    /**
     * Return a book using native SQL
     */
    @Modifying
    @Query(value = """
        UPDATE borrowing_records SET 
            return_date = CURRENT_DATE,
            status = 'RETURNED',
            returned_to = :returnedTo,
            fine_amount = CASE 
                WHEN due_date < CURRENT_DATE 
                THEN DATEDIFF(CURRENT_DATE, due_date) * :finePerDay
                ELSE 0 
            END,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = :recordId 
        AND status IN ('BORROWED', 'OVERDUE', 'RENEWED')
        """, nativeQuery = true)
    int returnBook(
            @Param("recordId") Long recordId,
            @Param("returnedTo") String returnedTo,
            @Param("finePerDay") double finePerDay
    );

    /**
     * Renew a book borrowing using native SQL
     */
    @Modifying
    @Query(value = """
        UPDATE borrowing_records SET 
            due_date = DATE_ADD(due_date, INTERVAL :additionalDays DAY),
            renewal_count = renewal_count + 1,
            status = 'RENEWED',
            updated_at = CURRENT_TIMESTAMP
        WHERE id = :recordId 
        AND status = 'BORROWED'
        AND renewal_count < max_renewals_allowed
        AND due_date >= CURRENT_DATE
        """, nativeQuery = true)
    int renewBook(@Param("recordId") Long recordId, @Param("additionalDays") int additionalDays);

    // ============= Reporting Queries =============

    /**
     * Get comprehensive library statistics using native SQL
     */
    @Query(value = """
        SELECT 
            'TOTAL_BORROWS' as metric,
            COUNT(*) as value
        FROM borrowing_records
        UNION ALL
        SELECT 
            'ACTIVE_BORROWS' as metric,
            COUNT(*) as value
        FROM borrowing_records 
        WHERE status = 'BORROWED'
        UNION ALL
        SELECT 
            'OVERDUE_BORROWS' as metric,
            COUNT(*) as value
        FROM borrowing_records 
        WHERE status = 'OVERDUE'
        UNION ALL
        SELECT 
            'TOTAL_FINES' as metric,
            COALESCE(SUM(fine_amount), 0) as value
        FROM borrowing_records 
        WHERE fine_amount > 0
        """, nativeQuery = true)
    List<Object[]> getLibraryStatistics();

    // ============= JPA Query Methods =============

    @Query("SELECT br FROM BorrowingRecord br WHERE br.user.id = :userId AND br.status = 'BORROWED'")
    List<BorrowingRecord> findActiveUserBorrowings(@Param("userId") Long userId);

    @Query("SELECT COUNT(br) FROM BorrowingRecord br WHERE br.user.id = :userId AND br.status = 'BORROWED'")
    long countActiveUserBorrowings(@Param("userId") Long userId);

    @Query("SELECT br FROM BorrowingRecord br WHERE br.dueDate < :date AND br.status IN ('BORROWED', 'OVERDUE')")
    List<BorrowingRecord> findOverdueRecords(@Param("date") LocalDate date);
}