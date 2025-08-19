package com.library.library_management_system.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * JWT Configuration properties using Lombok
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
@Data
@Validated
public class JwtConfig {

    @NotBlank(message = "JWT secret is required")
    private String secret;

    @NotNull(message = "JWT expiration time is required")
    @Positive(message = "JWT expiration must be positive")
    private Long expiration = 86400000L; // 24 hours in milliseconds

    @NotNull(message = "JWT refresh expiration time is required")
    @Positive(message = "JWT refresh expiration must be positive")
    private Long refreshExpiration = 604800000L; // 7 days in milliseconds

    @NotBlank(message = "JWT issuer is required")
    private String issuer = "library-management-system";

    @NotBlank(message = "JWT header is required")
    private String header = "Authorization";

    @NotBlank(message = "JWT token prefix is required")
    private String tokenPrefix = "Bearer ";

    // Algorithm configuration
    private String algorithm = "HS512";

    // Token validation settings
    private boolean validateExpiration = true;
    private boolean validateIssuer = true;
    private boolean validateAudience = false;

    // Clock skew in seconds (to handle time differences between servers)
    private long clockSkew = 60L;

    // Whether to include user details in token
    private boolean includeUserDetails = true;

    // Custom claims configuration
    private Claims claims = new Claims();

    @Data
    public static class Claims {
        private String userIdClaim = "user_id";
        private String usernameClaim = "username";
        private String roleClaim = "role";
        private String emailClaim = "email";
        private String fullNameClaim = "full_name";
        private String permissionsClaim = "permissions";
        private String isActiveClaim = "is_active";
    }

    // Helper methods
    public long getExpirationInSeconds() {
        return expiration / 1000;
    }

    public long getRefreshExpirationInSeconds() {
        return refreshExpiration / 1000;
    }

    public String getTokenWithoutPrefix(String token) {
        if (token != null && token.startsWith(tokenPrefix)) {
            return token.substring(tokenPrefix.length());
        }
        return token;
    }

    public String getTokenWithPrefix(String token) {
        if (token != null && !token.startsWith(tokenPrefix)) {
            return tokenPrefix + token;
        }
        return token;
    }
}