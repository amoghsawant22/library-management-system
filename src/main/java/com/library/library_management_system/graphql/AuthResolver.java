package com.library.library_management_system.graphql;

import com.library.library_management_system.dto.request.LoginRequest;
import com.library.library_management_system.dto.request.RegisterRequest;
import com.library.library_management_system.dto.response.AuthResponse;
import com.library.library_management_system.dto.response.UserResponse;
import com.library.library_management_system.enums.UserRole;
import com.library.library_management_system.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthResolver {

    private final AuthService authService;

    @MutationMapping
    public UserResponse register(@Argument Map<String, Object> input) {
        log.info("GraphQL: User registration for email: {}", input.get("email"));

        RegisterRequest request = RegisterRequest.builder()
                .username((String) input.get("username"))
                .fullName((String) input.get("fullName"))
                .email((String) input.get("email"))
                .password((String) input.get("password"))
                .confirmPassword((String) input.get("confirmPassword"))
                .role(input.containsKey("role") ?
                        UserRole.valueOf((String) input.get("role")) : UserRole.MEMBER)
                .phoneNumber((String) input.get("phoneNumber"))
                .address((String) input.get("address"))
                .build();

        return authService.register(request);
    }

    @MutationMapping
    public Map<String, Object> login(@Argument Map<String, Object> input) {
        log.info("GraphQL: Login attempt for identifier: {}", input.get("identifier"));

        LoginRequest request = LoginRequest.builder()
                .identifier((String) input.get("identifier"))
                .password((String) input.get("password"))
                .build();

        AuthResponse authResponse = authService.login(request);

        return Map.of(
                "accessToken", authResponse.getAccessToken(),
                "refreshToken", authResponse.getRefreshToken(),
                "tokenType", authResponse.getTokenType(),
                "expiresIn", authResponse.getExpiresIn(),
                "user", Map.of(
                        "id", authResponse.getUserId(),
                        "username", authResponse.getUsername(),
                        "fullName", authResponse.getFullName(),
                        "email", authResponse.getEmail(),
                        "role", authResponse.getRole()
                )
        );
    }

    @MutationMapping
    @PreAuthorize("hasRole('MEMBER') or hasRole('ADMIN')")
    public Boolean logout() {
        log.info("GraphQL: User logout");
        authService.logout(null);
        return true;
    }

    @QueryMapping
    public Boolean validateToken(@Argument String token) {
        log.debug("GraphQL: Validate token");
        return authService.isTokenValid(token);
    }

    @QueryMapping
    public Boolean checkEmailAvailability(@Argument String email) {
        log.debug("GraphQL: Check email availability: {}", email);
        return !authService.existsByEmail(email);
    }

    @QueryMapping
    public Boolean checkUsernameAvailability(@Argument String username) {
        log.debug("GraphQL: Check username availability: {}", username);
        return !authService.existsByUsername(username);
    }
}
