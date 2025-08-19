package com.library.library_management_system.service.impl;

import com.library.library_management_system.dto.mapper.UserMapper;
import com.library.library_management_system.dto.request.ChangePasswordRequest;
import com.library.library_management_system.dto.request.LoginRequest;
import com.library.library_management_system.dto.request.RegisterRequest;
import com.library.library_management_system.dto.response.AuthResponse;
import com.library.library_management_system.dto.response.UserResponse;
import com.library.library_management_system.entity.User;
import com.library.library_management_system.exception.BadRequestException;
import com.library.library_management_system.exception.ResourceNotFoundException;
import com.library.library_management_system.exception.UnauthorizedException;
import com.library.library_management_system.repository.UserRepository;
import com.library.library_management_system.security.JwtTokenProvider;
import com.library.library_management_system.security.UserPrincipal;
import com.library.library_management_system.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Authentication Service Implementation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        // Validate passwords match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already in use");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken");
        }

        // Create user entity
        User user = UserMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Save user
        User savedUser = userRepository.save(user);

        log.info("User registered successfully: {}", savedUser.getUsername());
        return UserMapper.toResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for identifier: {}", request.getIdentifier());

        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getIdentifier(),
                            request.getPassword()
                    )
            );

            // Generate tokens
            String accessToken = tokenProvider.generateToken(authentication);
            String refreshToken = tokenProvider.generateRefreshToken(authentication);

            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

            log.info("User logged in successfully: {}", userPrincipal.getUsername());

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(tokenProvider.getExpirationTimeFromToken(accessToken) / 1000)
                    .issuedAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusSeconds(tokenProvider.getExpirationTimeFromToken(accessToken) / 1000))
                    .userId(userPrincipal.getId())
                    .username(userPrincipal.getUsername())
                    .fullName(userPrincipal.getFullName())
                    .email(userPrincipal.getEmail())
                    .role(userPrincipal.getRole())
                    .isActive(userPrincipal.isEnabled())
                    .build();

        } catch (Exception e) {
            log.error("Login failed for identifier: {}", request.getIdentifier(), e);
            throw new UnauthorizedException("Invalid credentials");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(String refreshToken) {
        log.info("Refreshing access token");

        try {
            if (!tokenProvider.validateToken(refreshToken)) {
                throw new UnauthorizedException("Invalid refresh token");
            }

            String username = tokenProvider.getUsernameFromToken(refreshToken);
            User user = userRepository.findByEmailOrUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            if (!user.getIsActive()) {
                throw new UnauthorizedException("User account is deactivated");
            }

            // Create authentication object
            UserPrincipal userPrincipal = UserPrincipal.create(user);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userPrincipal, null, userPrincipal.getAuthorities());

            // Generate new tokens
            String newAccessToken = tokenProvider.generateToken(authentication);
            String newRefreshToken = tokenProvider.generateRefreshToken(authentication);

            log.info("Tokens refreshed for user: {}", username);

            return AuthResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .tokenType("Bearer")
                    .expiresIn(tokenProvider.getExpirationTimeFromToken(newAccessToken) / 1000)
                    .issuedAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusSeconds(tokenProvider.getExpirationTimeFromToken(newAccessToken) / 1000))
                    .userId(userPrincipal.getId())
                    .username(userPrincipal.getUsername())
                    .fullName(userPrincipal.getFullName())
                    .email(userPrincipal.getEmail())
                    .role(userPrincipal.getRole())
                    .isActive(userPrincipal.isEnabled())
                    .build();

        } catch (Exception e) {
            log.error("Token refresh failed", e);
            throw new UnauthorizedException("Invalid refresh token");
        }
    }

    @Override
    public void logout(String token) {
        log.info("User logging out");
        // In a real implementation, you might want to maintain a blacklist of tokens
        // For now, we'll just clear the security context
        SecurityContextHolder.clearContext();
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        log.info("Changing password for user ID: {}", userId);

        // Validate passwords match
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new BadRequestException("New passwords do not match");
        }

        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password changed successfully for user: {}", user.getUsername());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return UserMapper.toResponse(user);
    }

    @Override
    public boolean isTokenValid(String token) {
        return tokenProvider.validateToken(token);
    }
}