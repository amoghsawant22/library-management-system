package com.library.library_management_system.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    @NotBlank(message = "Email or username is required")
    private String identifier; // Can be email or username

    @NotBlank(message = "Password is required")
    private String password;
}