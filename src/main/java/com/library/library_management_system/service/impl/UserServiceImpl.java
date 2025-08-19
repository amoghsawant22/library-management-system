package com.library.library_management_system.service.impl;

import com.library.library_management_system.dto.mapper.PageMapper;
import com.library.library_management_system.dto.mapper.UserMapper;
import com.library.library_management_system.dto.request.UserSearchRequest;
import com.library.library_management_system.dto.request.UserUpdateRequest;
import com.library.library_management_system.dto.response.PagedResponse;
import com.library.library_management_system.dto.response.UserResponse;
import com.library.library_management_system.entity.User;
import com.library.library_management_system.enums.UserRole;
import com.library.library_management_system.exception.BadRequestException;
import com.library.library_management_system.exception.ResourceNotFoundException;
import com.library.library_management_system.exception.UnauthorizedException;
import com.library.library_management_system.repository.BorrowingRecordRepository;
import com.library.library_management_system.repository.UserRepository;
import com.library.library_management_system.security.UserPrincipal;
import com.library.library_management_system.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * User Service Implementation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BorrowingRecordRepository borrowingRecordRepository;

    @Override
    @Cacheable(value = "users", key = "#id")
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        log.debug("Getting user by ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        return UserMapper.toResponse(user);
    }

    @Override
    @Cacheable(value = "users", key = "#username")
    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        log.debug("Getting user by username: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        return UserMapper.toResponse(user);
    }

    @Override
    @Cacheable(value = "users", key = "#email")
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        log.debug("Getting user by email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        return UserMapper.toResponse(user);
    }

    @Override
    @CacheEvict(value = "users", key = "#id")
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        log.info("Updating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        // Validate email uniqueness if changed
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BadRequestException("Email is already in use");
            }
        }

        UserMapper.updateEntityFromRequest(user, request);
        User updatedUser = userRepository.save(user);

        log.info("User updated successfully: {}", updatedUser.getUsername());
        return UserMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional
    public UserResponse updateCurrentUserProfile(UserUpdateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        // Members can only update certain fields
        if (userPrincipal.getRole() == UserRole.MEMBER) {
            request.setRole(null); // Members cannot change their role
            request.setIsActive(null); // Members cannot change their active status
            request.setMaxBooksAllowed(null); // Members cannot change their book limit
        }

        return updateUser(userPrincipal.getId(), request);
    }

    @Override
    @CacheEvict(value = "users", key = "#id")
    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        // Check if user has active borrowings
        long activeBorrowings = borrowingRecordRepository.countActiveUserBorrowings(id);
        if (activeBorrowings > 0) {
            throw new BadRequestException("Cannot delete user with active book borrowings");
        }

        // Soft delete by deactivating
        user.setIsActive(false);
        userRepository.save(user);

        log.info("User soft deleted: {}", user.getUsername());
    }

    @Override
    @CacheEvict(value = "users", key = "#id")
    @Transactional
    public void toggleUserStatus(Long id, boolean isActive) {
        log.info("Toggling user status for ID: {} to {}", id, isActive);

        int updated = userRepository.updateUserActiveStatus(id, isActive, getCurrentUsername());

        if (updated == 0) {
            throw new ResourceNotFoundException("User not found with ID: " + id);
        }

        log.info("User status updated successfully for ID: {}", id);
    }

    @Override
    @Cacheable(value = "users")
    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> getAllUsers(int page, int size, String sortBy, String sortDirection) {
        log.debug("Getting all users - page: {}, size: {}, sortBy: {}, direction: {}",
                page, size, sortBy, sortDirection);

        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<User> userPage = userRepository.findAll(pageable);

        return PageMapper.toPagedResponse(userPage, UserMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> searchUsers(UserSearchRequest request) {
        log.debug("Searching users with filters: {}", request);

        Sort.Direction direction = request.getSortDirection().equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(),
                Sort.by(direction, request.getSortBy()));

        Page<User> userPage;

        if (request.getSearchTerm() != null && !request.getSearchTerm().trim().isEmpty()) {
            userPage = userRepository.searchUsers(request.getSearchTerm(), pageable);
        } else if (request.getRole() != null) {
            userPage = userRepository.findActiveUsersByRole(request.getRole().name(), pageable);
        } else {
            userPage = userRepository.findActiveUsersByRole(null, pageable);
        }

        return PageMapper.toPagedResponse(userPage, UserMapper::toResponse);
    }

    @Override
    @Cacheable(value = "users", key = "#role.name()")
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByRole(UserRole role) {
        log.debug("Getting users by role: {}", role);

        List<User> users = userRepository.findByRole(role);
        return users.stream().map(UserMapper::toResponse).toList();
    }

    @Override
    @Cacheable(value = "users", key = "'active'")
    @Transactional(readOnly = true)
    public List<UserResponse> getActiveUsers() {
        log.debug("Getting active users");

        List<User> users = userRepository.findByIsActiveTrue();
        return users.stream().map(UserMapper::toResponse).toList();
    }

    @Override
    @Cacheable(value = "users", key = "'inactive'")
    @Transactional(readOnly = true)
    public List<UserResponse> getInactiveUsers() {
        log.debug("Getting inactive users");

        List<User> users = userRepository.findByIsActiveFalse();
        return users.stream().map(UserMapper::toResponse).toList();
    }

    @Override
    @Cacheable(value = "userStats", key = "'overdueUsers'")
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersWithOverdueBooks() {
        log.debug("Getting users with overdue books");

        List<User> users = userRepository.findUsersWithOverdueBooks();
        return users.stream().map(UserMapper::toResponse).toList();
    }

    @Override
    @Cacheable(value = "userStats", key = "'mostActive:' + #limit")
    @Transactional(readOnly = true)
    public List<UserResponse> getMostActiveUsers(int limit) {
        log.debug("Getting most active users, limit: {}", limit);

        List<User> users = userRepository.findMostActiveUsers(limit);
        return users.stream().map(UserMapper::toResponse).toList();
    }

    @Override
    @Cacheable(value = "userStats", key = "'nearLimit'")
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersNearBookLimit() {
        log.debug("Getting users near book limit");

        List<User> users = userRepository.findUsersNearBookLimit();
        return users.stream().map(UserMapper::toResponse).toList();
    }

    @Override
    @Cacheable(value = "users", key = "'recent:' + #sinceDate.toString()")
    @Transactional(readOnly = true)
    public List<UserResponse> getRecentMembers(LocalDate sinceDate) {
        log.debug("Getting recent members since: {}", sinceDate);

        List<User> users = userRepository.findRecentMembers(sinceDate);
        return users.stream().map(UserMapper::toResponse).toList();
    }

    @Override
    @Cacheable(value = "userStats", key = "#userId")
    @Transactional(readOnly = true)
    public UserResponse getUserWithStats(Long userId) {
        log.debug("Getting user with stats for ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Object[] stats = userRepository.getUserBorrowingStats(userId);

        if (stats != null && stats.length >= 7) {
            Long totalBorrowed = ((Number) stats[4]).longValue();
            Long totalReturned = ((Number) stats[5]).longValue();
            Long overdueCount = ((Number) stats[6]).longValue();
            Double totalFines = ((Number) stats[7]).doubleValue();

            return UserMapper.toResponseWithStats(user, totalBorrowed, totalReturned, overdueCount, totalFines);
        }

        return UserMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUsersByRole(UserRole role) {
        return userRepository.countActiveUsersByRole(role);
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveUsers() {
        return userRepository.countActiveUsersByRole(null);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canUserBorrowBooks(Long userId) {
        Object[] capacity = borrowingRecordRepository.getUserBorrowingCapacity(userId);

        if (capacity != null && capacity.length >= 2) {
            Integer maxAllowed = (Integer) capacity[0];
            Long currentBorrowed = ((Number) capacity[1]).longValue();

            return currentBorrowed < maxAllowed;
        }

        return false;
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "system";
    }
}