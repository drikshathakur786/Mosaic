package com.mosaic.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * Standardized error response returned by all API endpoints.
 *
 * Every error from Mosaic follows this structure so clients
 * can parse errors consistently without guessing the format.
 *
 * Example:
 * {
 *   "status": 400,
 *   "error": "Bad Request",
 *   "code": "VALIDATION_ERROR",
 *   "message": "Validation failed",
 *   "details": [{"field": "email", "message": "must not be blank"}],
 *   "timestamp": "2024-03-15T10:30:00Z",
 *   "path": "/api/v1/auth/register",
 *   "correlationId": "abc-123"
 * }
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        int status,
        String error,
        String code,
        String message,
        List<FieldError> details,
        Instant timestamp,
        String path,
        String correlationId
) {

    /**
     * Represents a single field-level validation error.
     */
    public record FieldError(
            String field,
            String message
    ) {}

    /**
     * Factory method for creating an error response without field-level details.
     */
    public static ErrorResponse of(int status, String error, String code, String message,
                                   String path, String correlationId) {
        return new ErrorResponse(status, error, code, message, null,
                Instant.now(), path, correlationId);
    }

    /**
     * Factory method for creating a validation error response with field details.
     */
    public static ErrorResponse ofValidation(String message, List<FieldError> details,
                                             String path, String correlationId) {
        return new ErrorResponse(400, "Bad Request", "VALIDATION_ERROR", message,
                details, Instant.now(), path, correlationId);
    }
}
