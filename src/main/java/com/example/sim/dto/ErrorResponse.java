package com.example.sim.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Error Response DTO
 * Standardized error format for all API errors
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    /**
     * HTTP status code
     */
    private int status;

    /**
     * Error message
     */
    private String message;

    /**
     * Detailed error description
     */
    private String details;

    /**
     * Timestamp when error occurred
     */
    private LocalDateTime timestamp;

    /**
     * Request path where error occurred
     */
    private String path;

    public ErrorResponse(int status, String message, String details, String path) {
        this.status = status;
        this.message = message;
        this.details = details;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }
}