package com.library.library_management_system.dto.mapper;

import com.library.library_management_system.dto.request.RegisterRequest;
import com.library.library_management_system.dto.request.UserUpdateRequest;
import com.library.library_management_system.dto.response.UserResponse;
import com.library.library_management_system.entity.User;
import lombok.experimental.UtilityClass;

import java.time.LocalDate;

@UtilityClass
public class UserMapper {

    public static User toEntity(RegisterRequest request) {
        return User.builder()
                .username(request.getUsername())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(request.getPassword()) // Will be encoded by service
                .role(request.getRole())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .dateOfBirth(request.getDateOfBirth())
                .membershipDate(LocalDate.now())
                .isActive(true)
                .build();
    }

    public static UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .dateOfBirth(user.getDateOfBirth())
                .membershipDate(user.getMembershipDate())
                .isActive(user.getIsActive())
                .maxBooksAllowed(user.getMaxBooksAllowed())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public static UserResponse toResponseWithStats(User user, Long totalBorrowed,
                                                   Long currentlyBorrowed, Long overdue, Double totalFines) {
        UserResponse response = toResponse(user);
        response.setTotalBorrowedBooks(totalBorrowed);
        response.setCurrentlyBorrowedBooks(currentlyBorrowed);
        response.setOverdueBooks(overdue);
        response.setTotalFines(totalFines);
        return response;
    }

    public static void updateEntityFromRequest(User user, UserUpdateRequest request) {
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }
        if (request.getDateOfBirth() != null) {
            user.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }
        if (request.getMaxBooksAllowed() != null) {
            user.setMaxBooksAllowed(request.getMaxBooksAllowed());
        }
    }
}