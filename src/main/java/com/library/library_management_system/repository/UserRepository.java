package com.library.library_management_system.repository;

import com.library.library_management_system.entity.User;
import com.library.library_management_system.enums.UserRole;
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
 * Repository for User entity with custom queries
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // ============= Basic CRUD Operations =============

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    // ============= Native SQL Queries =============

    /**
     * Find user by email or username using native SQL
     */
    @Query(value = """
        SELECT * FROM users u 
        WHERE (u.email = :identifier OR u.username = :identifier) 
        AND u.is_active = true
        """, nativeQuery = true)
    Optional<User> findByEmailOrUsername(@Param("identifier") String identifier);

    /**
     * Get all active users with role filter using native SQL
     */
    @Query(value = """
        SELECT * FROM users u 
        WHERE u.is_active = true 
        AND (:role IS NULL OR u.role = :role)
        ORDER BY u.created_at DESC
        """, nativeQuery = true)
    Page<User> findActiveUsersByRole(@Param("role") String role, Pageable pageable);

    /**
     * Find users who joined in a specific date range
     */
    @Query(value = """
        SELECT * FROM users u 
        WHERE u.membership_date BETWEEN :startDate AND :endDate
        AND u.is_active = true
        ORDER BY u.membership_date DESC
        """, nativeQuery = true)
    List<User> findUsersByMembershipDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Get most active users based on borrowing count using native SQL
     */
    @Query(value = """
        SELECT u.*, COUNT(br.id) as borrow_count
        FROM users u 
        LEFT JOIN borrowing_records br ON u.id = br.user_id
        WHERE u.is_active = true
        GROUP BY u.id
        ORDER BY borrow_count DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<User> findMostActiveUsers(@Param("limit") int limit);

    /**
     * Get users with overdue books using native SQL
     */
    @Query(value = """
        SELECT DISTINCT u.* 
        FROM users u 
        INNER JOIN borrowing_records br ON u.id = br.user_id
        WHERE br.status IN ('BORROWED', 'OVERDUE') 
        AND br.due_date < CURRENT_DATE
        AND u.is_active = true
        ORDER BY u.full_name
        """, nativeQuery = true)
    List<User> findUsersWithOverdueBooks();

    /**
     * Get user borrowing statistics using native SQL
     */
    @Query(value = """
        SELECT 
            u.id,
            u.username,
            u.full_name,
            COUNT(CASE WHEN br.status = 'BORROWED' THEN 1 END) as currently_borrowed,
            COUNT(CASE WHEN br.status = 'RETURNED' THEN 1 END) as total_returned,
            COUNT(CASE WHEN br.status = 'OVERDUE' THEN 1 END) as overdue_count,
            COALESCE(SUM(br.fine_amount), 0) as total_fines
        FROM users u 
        LEFT JOIN borrowing_records br ON u.id = br.user_id
        WHERE u.id = :userId
        GROUP BY u.id, u.username, u.full_name
        """, nativeQuery = true)
    Object[] getUserBorrowingStats(@Param("userId") Long userId);

    /**
     * Update user active status using native SQL
     */
    @Modifying
    @Query(value = """
        UPDATE users SET 
            is_active = :isActive,
            updated_at = CURRENT_TIMESTAMP,
            updated_by = :updatedBy
        WHERE id = :userId
        """, nativeQuery = true)
    int updateUserActiveStatus(
            @Param("userId") Long userId,
            @Param("isActive") boolean isActive,
            @Param("updatedBy") String updatedBy
    );

    /**
     * Find users approaching max book limit using native SQL
     */
    @Query(value = """
        SELECT u.*, COUNT(br.id) as current_borrowed
        FROM users u 
        LEFT JOIN borrowing_records br ON u.id = br.user_id 
            AND br.status = 'BORROWED'
        WHERE u.is_active = true
        GROUP BY u.id
        HAVING current_borrowed >= (u.max_books_allowed * 0.8)
        ORDER BY current_borrowed DESC
        """, nativeQuery = true)
    List<User> findUsersNearBookLimit();

    /**
     * Search users by name or email with pagination using native SQL
     */
    @Query(value = """
        SELECT * FROM users u
        WHERE u.is_active = true
        AND (
            LOWER(u.full_name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) 
            OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
        )
        ORDER BY u.full_name
        """, nativeQuery = true)
    Page<User> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);

    // ============= JPA Query Methods =============

    List<User> findByRole(UserRole role);

    List<User> findByIsActiveTrue();

    List<User> findByIsActiveFalse();

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.isActive = true")
    long countActiveUsersByRole(@Param("role") UserRole role);

    @Query("SELECT u FROM User u WHERE u.membershipDate >= :date AND u.isActive = true")
    List<User> findRecentMembers(@Param("date") LocalDate date);
}