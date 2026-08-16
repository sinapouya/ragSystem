package com.example.ragApplication.exception;

import java.time.LocalDateTime;

/**
 * Standard error response format sent to the client.
 * Using a Record for immutable DTO (Java 21+).
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {}