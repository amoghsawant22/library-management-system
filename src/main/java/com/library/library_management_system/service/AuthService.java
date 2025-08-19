package com.library.library_management_system.service;

import com.library.library_management_system.dto.request.ChangePasswordRequest;
import com.library.library_management_system.dto.request.LoginRequest;
import com.library.library_management_system.dto.request.RegisterRequest;
import com.library.library_management_system.dto.response.AuthResponse;
import com.library.library_management_system.dto.response.UserResponse;

/**
 * Authentication Service Interface
 */
public interface AuthService {

    /**
     * Register a new user
     */
    UserResponse register(RegisterRequest request);

    /**
     * Authenticate user and generate tokens
     */
    AuthResponse login(LoginRequest request);

    /**
     * Refresh access token using refresh token
     */
    AuthResponse refreshToken(String refreshToken);

    /**
     * Logout user (invalidate tokens)
     */
    void logout(String token);

    /**
     * Change user password
     */
    void changePassword(Long userId, ChangePasswordRequest request);

    /**
     * Validate if email already exists
     */
    boolean existsByEmail(String email);
    /**
     * Validate if username already exists
     */
    boolean existsByUsername(String username);

    /**
     * Get current authenticated user
     */
    UserResponse getCurrentUser();

    /**
     * Verify JWT token validity
     */
    boolean isTokenValid(String token);
}