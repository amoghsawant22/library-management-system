package com.library.library_management_system.dto.response;

import com.library.library_management_system.enums.UserRole;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;

    // User info
    private Long userId;
    private String username;
    private String fullName;
    private String email;
    private UserRole role;
    private Boolean isActive;
}