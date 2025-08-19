package com.library.library_management_system.controller;

import com.library.library_management_system.dto.request.UserSearchRequest;
import com.library.library_management_system.dto.request.UserUpdateRequest;
import com.library.library_management_system.dto.response.ApiResponse;
import com.library.library_management_system.dto.response.PagedResponse;
import com.library.library_management_system.dto.response.UserResponse;
import com.library.library_management_system.enums.UserRole;
import com.library.library_management_system.service.UserService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * User Management Controller
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "User management and profile endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get all users", description = "Get all users with pagination (Admin only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Users retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PagedResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Admin role required")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> getAllUsers(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "fullName") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)")
            @RequestParam(defaultValue = "asc") String sortDirection) {

        log.debug("Get all users request - page: {}, size: {}, sortBy: {}", page, size, sortBy);

        PagedResponse<UserResponse> users = userService.getAllUsers(page, size, sortBy, sortDirection);

        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @Operation(summary = "Search users", description = "Search users with filters (Admin only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Search results",
                    content = @Content(schema = @Schema(implementation = PagedResponse.class)))
    })
    @PostMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> searchUsers(
            @Parameter(description = "Search criteria", required = true)
            @Valid @RequestBody UserSearchRequest request) {

        log.debug("User search request: {}", request);

        PagedResponse<UserResponse> users = userService.searchUsers(request);

        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @Operation(summary = "Get user by ID", description = "Get user details by ID (Admin only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "User found",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "User not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long id) {

        log.debug("Get user by ID: {}", id);

        UserResponse user = userService.getUserById(id);

        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @Operation(summary = "Get user with statistics", description = "Get user details with borrowing statistics (Admin only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "User with statistics",
                    content = @Content(schema = @Schema(implementation = UserResponse.class)))
    })
    @GetMapping("/{id}/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserWithStats(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long id) {

        log.debug("Get user with stats by ID: {}", id);

        UserResponse user = userService.getUserWithStats(id);

        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @Operation(summary = "Update user", description = "Update user details (Admin only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "User updated successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "User not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Email already exists")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "User update details", required = true)
            @Valid @RequestBody UserUpdateRequest request) {

        log.info("Update user request for ID: {}", id);

        UserResponse user = userService.updateUser(id, request);

        return ResponseEntity.ok(ApiResponse.success(user, "User updated successfully"));
    }

    @Operation(summary = "Delete user", description = "Soft delete user (Admin only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "User deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "User not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Cannot delete user with active borrowings")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long id) {

        log.info("Delete user request for ID: {}", id);

        userService.deleteUser(id);

        return ResponseEntity.ok(ApiResponse.success(null, "User deleted successfully"));
    }

    @Operation(summary = "Toggle user status", description = "Activate/deactivate user (Admin only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "User status updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "User not found")
    })
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> toggleUserStatus(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "New active status", required = true)
            @RequestParam boolean isActive) {

        log.info("Toggle user status for ID: {} to {}", id, isActive);

        userService.toggleUserStatus(id, isActive);

        return ResponseEntity.ok(ApiResponse.success(null,
                String.format("User %s successfully", isActive ? "activated" : "deactivated")));
    }

    // ============= Profile Management =============

    @Operation(summary = "Get current user profile", description = "Get current authenticated user's profile")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "User profile",
                    content = @Content(schema = @Schema(implementation = UserResponse.class)))
    })
    @GetMapping("/profile")
    @PreAuthorize("hasRole('MEMBER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUserProfile() {

        log.debug("Get current user profile request");

        UserResponse user = userService.getUserWithStats(getCurrentUserId());

        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @Operation(summary = "Update current user profile", description = "Update current authenticated user's profile")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Profile updated successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Email already exists")
    })
    @PutMapping("/profile")
    @PreAuthorize("hasRole('MEMBER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateCurrentUserProfile(
            @Parameter(description = "Profile update details", required = true)
            @Valid @RequestBody UserUpdateRequest request) {

        log.info("Update current user profile request");

        UserResponse user = userService.updateCurrentUserProfile(request);

        return ResponseEntity.ok(ApiResponse.success(user, "Profile updated successfully"));
    }

    // ============= User Statistics and Reports =============

    @Operation(summary = "Get users by role", description = "Get users filtered by role (Admin only)")
    @GetMapping("/by-role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersByRole(
            @Parameter(description = "User role", required = true)
            @PathVariable UserRole role) {

        log.debug("Get users by role: {}", role);

        List<UserResponse> users = userService.getUsersByRole(role);

        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @Operation(summary = "Get active users", description = "Get all active users (Admin only)")
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getActiveUsers() {

        log.debug("Get active users request");

        List<UserResponse> users = userService.getActiveUsers();

        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @Operation(summary = "Get inactive users", description = "Get all inactive users (Admin only)")
    @GetMapping("/inactive")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getInactiveUsers() {

        log.debug("Get inactive users request");

        List<UserResponse> users = userService.getInactiveUsers();

        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @Operation(summary = "Get users with overdue books", description = "Get users who have overdue books (Admin only)")
    @GetMapping("/overdue-books")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersWithOverdueBooks() {

        log.debug("Get users with overdue books request");

        List<UserResponse> users = userService.getUsersWithOverdueBooks();

        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @Operation(summary = "Get most active users", description = "Get most active users by borrowing activity (Admin only)")
    @GetMapping("/most-active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getMostActiveUsers(
            @Parameter(description = "Number of users to return")
            @RequestParam(defaultValue = "10") int limit) {

        log.debug("Get most active users request - limit: {}", limit);

        List<UserResponse> users = userService.getMostActiveUsers(limit);

        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @Operation(summary = "Get users near book limit", description = "Get users approaching their borrowing limit (Admin only)")
    @GetMapping("/near-limit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersNearBookLimit() {

        log.debug("Get users near book limit request");

        List<UserResponse> users = userService.getUsersNearBookLimit();

        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @Operation(summary = "Get recent members", description = "Get users who joined recently (Admin only)")
    @GetMapping("/recent-members")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getRecentMembers(
            @Parameter(description = "Since date (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sinceDate) {

        log.debug("Get recent members since: {}", sinceDate);

        List<UserResponse> users = userService.getRecentMembers(sinceDate);

        return ResponseEntity.ok(ApiResponse.success(users));
    }

    // ============= User Statistics =============

    @Operation(summary = "Get user count by role", description = "Get count of users by role (Admin only)")
    @GetMapping("/count/by-role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Long>> getUserCountByRole(
            @Parameter(description = "User role", required = true)
            @PathVariable UserRole role) {

        log.debug("Get user count by role: {}", role);

        long count = userService.countUsersByRole(role);

        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @Operation(summary = "Get active user count", description = "Get total count of active users (Admin only)")
    @GetMapping("/count/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Long>> getActiveUserCount() {

        log.debug("Get active user count request");

        long count = userService.countActiveUsers();

        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @Operation(summary = "Check user borrowing eligibility", description = "Check if user can borrow more books")
    @GetMapping("/{id}/can-borrow")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MEMBER') and #id == authentication.principal.id)")
    public ResponseEntity<ApiResponse<Boolean>> canUserBorrowBooks(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long id) {

        log.debug("Check user borrowing eligibility for ID: {}", id);

        boolean canBorrow = userService.canUserBorrowBooks(id);

        return ResponseEntity.ok(ApiResponse.success(canBorrow,
                canBorrow ? "User can borrow books" : "User has reached borrowing limit"));
    }

    // Helper method to get current user ID (would typically be implemented in a base controller)
    private Long getCurrentUserId() {
        // This would extract user ID from security context
        // Implementation depends on your security setup
        return 1L; // Placeholder
    }
}