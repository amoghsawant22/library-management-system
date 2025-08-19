package com.library.library_management_system.service;

import com.library.library_management_system.dto.request.UserSearchRequest;
import com.library.library_management_system.dto.request.UserUpdateRequest;
import com.library.library_management_system.dto.response.PagedResponse;
import com.library.library_management_system.dto.response.UserResponse;
import com.library.library_management_system.enums.UserRole;

import java.time.LocalDate;
import java.util.List;

/**
 * User Service Interface
 */
public interface UserService {

    /**
     * Get user by ID
     */
    UserResponse getUserById(Long id);

    /**
     * Get user by username
     */
    UserResponse getUserByUsername(String username);

    /**
     * Get user by email
     */
    UserResponse getUserByEmail(String email);

    /**
     * Update user profile
     */
    UserResponse updateUser(Long id, UserUpdateRequest request);

    /**
     * Update current user's profile
     */
    UserResponse updateCurrentUserProfile(UserUpdateRequest request);

    /**
     * Delete user (soft delete)
     */
    void deleteUser(Long id);

    /**
     * Activate/Deactivate user
     */
    void toggleUserStatus(Long id, boolean isActive);

    /**
     * Get all users with pagination
     */
    PagedResponse<UserResponse> getAllUsers(int page, int size, String sortBy, String sortDirection);

    /**
     * Search users with filters
     */
    PagedResponse<UserResponse> searchUsers(UserSearchRequest request);

    /**
     * Get users by role
     */
    List<UserResponse> getUsersByRole(UserRole role);

    /**
     * Get active users
     */
    List<UserResponse> getActiveUsers();

    /**
     * Get inactive users
     */
    List<UserResponse> getInactiveUsers();

    /**
     * Get users with overdue books
     */
    List<UserResponse> getUsersWithOverdueBooks();

    /**
     * Get most active users (by borrowing activity)
     */
    List<UserResponse> getMostActiveUsers(int limit);

    /**
     * Get users near book borrowing limit
     */
    List<UserResponse> getUsersNearBookLimit();

    /**
     * Get new members (joined recently)
     */
    List<UserResponse> getRecentMembers(LocalDate sinceDate);

    /**
     * Get user statistics
     */
    UserResponse getUserWithStats(Long userId);

    /**
     * Count users by role
     */
    long countUsersByRole(UserRole role);

    /**
     * Count active users
     */
    long countActiveUsers();

    /**
     * Check if user exists
     */
    boolean existsById(Long id);

    /**
     * Check if user can borrow more books
     */
    boolean canUserBorrowBooks(Long userId);
}