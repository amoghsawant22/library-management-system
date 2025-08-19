package com.library.library_management_system.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {

    private Boolean success;
    private String message;
    private T data;
    private String error;
    private Integer status;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    // Success response
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .status(200)
                .build();
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .status(200)
                .build();
    }

    // Error response
    public static <T> ApiResponse<T> error(String error, Integer status) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(error)
                .status(status)
                .build();
    }

    public static <T> ApiResponse<T> error(String error) {
        return error(error, 400);
    }
}
