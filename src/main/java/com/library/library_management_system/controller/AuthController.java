package com.library.library_management_system.controller;

import com.library.library_management_system.dto.request.ChangePasswordRequest;
import com.library.library_management_system.dto.request.LoginRequest;
import com.library.library_management_system.dto.request.RegisterRequest;
import com.library.library_management_system.dto.response.ApiResponse;
import com.library.library_management_system.dto.response.AuthResponse;
import com.library.library_management_system.dto.response.UserResponse;
import com.library.library_management_system.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication and Authorization Controller
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication and user management endpoints")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register a new user", description = "Create a new user account in the system")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data or user already exists",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Email or username already exists",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Parameter(description = "User registration details", required = true)
            @Valid @RequestBody RegisterRequest request) {

        log.info("User registration attempt for email: {}", request.getEmail());

        UserResponse user = authService.register(request);

        return ResponseEntity.status(201)
                .body(ApiResponse.success(user, "User registered successfully"));
    }

    @Operation(summary = "User login", description = "Authenticate user and return JWT tokens")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Parameter(description = "User login credentials", required = true)
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        log.info("Login attempt for identifier: {} from IP: {}",
                request.getIdentifier(),
                getClientIpAddress(httpRequest));

        AuthResponse authResponse = authService.login(request);

        return ResponseEntity.ok(ApiResponse.success(authResponse, "Login successful"));
    }

    @Operation(summary = "Refresh access token", description = "Generate new access token using refresh token")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Token refreshed successfully",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Invalid or expired refresh token",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Parameter(description = "Refresh token", required = true)
            @RequestParam("refreshToken") String refreshToken) {

        log.debug("Token refresh request");

        AuthResponse authResponse = authService.refreshToken(refreshToken);

        return ResponseEntity.ok(ApiResponse.success(authResponse, "Token refreshed successfully"));
    }

    @Operation(summary = "User logout", description = "Invalidate current user session")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Logout successful",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping("/logout")
    @PreAuthorize("hasRole('MEMBER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Parameter(description = "Authorization header with Bearer token")
            @RequestHeader("Authorization") String authHeader) {

        log.info("User logout request");

        String token = authHeader.substring(7); // Remove "Bearer " prefix
        authService.logout(token);

        return ResponseEntity.ok(ApiResponse.success(null, "Logout successful"));
    }

    @Operation(summary = "Change password", description = "Change current user's password")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Password changed successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid current password or password validation failed",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping("/change-password")
    @PreAuthorize("hasRole('MEMBER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Parameter(description = "Password change details", required = true)
            @Valid @RequestBody ChangePasswordRequest request) {

        log.info("Password change request for current user");

        UserResponse currentUser = authService.getCurrentUser();
        authService.changePassword(currentUser.getId(), request);

        return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully"));
    }

    @Operation(summary = "Get current user", description = "Get current authenticated user details")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Current user details",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "User not authenticated",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/me")
    @PreAuthorize("hasRole('MEMBER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {

        log.debug("Get current user request");

        UserResponse user = authService.getCurrentUser();

        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @Operation(summary = "Validate token", description = "Check if the provided token is valid")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Token validation result",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping("/validate-token")
    public ResponseEntity<ApiResponse<Boolean>> validateToken(
            @Parameter(description = "JWT token to validate", required = true)
            @RequestParam("token") String token) {

        log.debug("Token validation request");

        boolean isValid = authService.isTokenValid(token);

        return ResponseEntity.ok(ApiResponse.success(isValid,
                isValid ? "Token is valid" : "Token is invalid"));
    }

    @Operation(summary = "Check email availability", description = "Check if email is available for registration")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Email availability status",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Boolean>> checkEmailAvailability(
            @Parameter(description = "Email to check", required = true)
            @RequestParam("email") String email) {

        log.debug("Email availability check for: {}", email);

        boolean exists = authService.existsByEmail(email);

        return ResponseEntity.ok(ApiResponse.success(!exists,
                exists ? "Email is already taken" : "Email is available"));
    }

    @Operation(summary = "Check username availability", description = "Check if username is available for registration")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Username availability status",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/check-username")
    public ResponseEntity<ApiResponse<Boolean>> checkUsernameAvailability(
            @Parameter(description = "Username to check", required = true)
            @RequestParam("username") String username) {

        log.debug("Username availability check for: {}", username);

        boolean exists = authService.existsByUsername(username);

        return ResponseEntity.ok(ApiResponse.success(!exists,
                exists ? "Username is already taken" : "Username is available"));
    }

    // Helper method to get client IP address
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0];
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}