package com.example.sim.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Generic API Response wrapper
 * Provides consistent response format
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    /**
     * Response status (success/error)
     */
    private String status;

    /**
     * Response message
     */
    private String message;

    /**
     * Actual data payload
     */
    private T data;

    /**
     * Timestamp
     */
    private LocalDateTime timestamp;

    /**
     * Success response
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>("success", message, data, LocalDateTime.now());
    }

    /**
     * Success response with default message
     */
    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Operation completed successfully");
    }

    /**
     * Error response
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>("error", message, null, LocalDateTime.now());
    }
}