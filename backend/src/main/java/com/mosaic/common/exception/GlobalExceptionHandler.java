package com.mosaic.common.exception;

import com.mosaic.common.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

/**
 * Global exception handler for the entire Mosaic API.
 *
 * Catches all exceptions thrown by controllers and services, and converts
 * them into a consistent {@link ErrorResponse} JSON structure.
 *
 * This ensures clients never receive raw stack traces or inconsistent
 * error formats, regardless of what goes wrong internally.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // =========================================================================
    // Custom Business Exceptions
    // =========================================================================

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex,
                                                         HttpServletRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND",
                ex.getMessage(), request);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateResourceException ex,
                                                          HttpServletRequest request) {
        log.warn("Duplicate resource: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, "DUPLICATE_RESOURCE",
                ex.getMessage(), request);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex,
                                                             HttpServletRequest request) {
        log.warn("Unauthorized access: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED",
                ex.getMessage(), request);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException ex,
                                                          HttpServletRequest request) {
        log.warn("Forbidden access: {}", ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, "FORBIDDEN",
                ex.getMessage(), request);
    }

    // =========================================================================
    // Validation Errors
    // =========================================================================

    /**
     * Handles @Valid / @Validated annotation failures on request bodies.
     * Returns a list of which fields failed and why.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                           HttpServletRequest request) {
        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> new ErrorResponse.FieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();

        log.warn("Validation failed on {} — {} error(s)", request.getRequestURI(), fieldErrors.size());

        ErrorResponse error = ErrorResponse.ofValidation(
                "Validation failed", fieldErrors,
                request.getRequestURI(), getCorrelationId()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex,
                                                             HttpServletRequest request) {
        log.warn("Missing request parameter: {}", ex.getParameterName());
        return buildResponse(HttpStatus.BAD_REQUEST, "MISSING_PARAMETER",
                ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                             HttpServletRequest request) {
        String message = String.format("Parameter '%s' must be of type %s",
                ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        log.warn("Type mismatch: {}", message);
        return buildResponse(HttpStatus.BAD_REQUEST, "TYPE_MISMATCH", message, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMalformedJson(HttpMessageNotReadableException ex,
                                                              HttpServletRequest request) {
        log.warn("Malformed JSON in request body");
        return buildResponse(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST",
                "Request body is missing or contains malformed JSON", request);
    }

    // =========================================================================
    // HTTP Errors
    // =========================================================================

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex,
                                                                 HttpServletRequest request) {
        return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED",
                ex.getMessage(), request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException ex,
                                                                HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "ENDPOINT_NOT_FOUND",
                "The requested endpoint does not exist", request);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleFileTooLarge(MaxUploadSizeExceededException ex,
                                                             HttpServletRequest request) {
        log.warn("File upload exceeded size limit");
        return buildResponse(HttpStatus.PAYLOAD_TOO_LARGE, "FILE_TOO_LARGE",
                "Uploaded file exceeds the maximum allowed size of 10MB", request);
    }

    // =========================================================================
    // Catch-All — Prevents raw stack traces from leaking
    // =========================================================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex,
                                                           HttpServletRequest request) {
        // Log the full stack trace internally, but never expose it to clients
        log.error("Unexpected error on {} {}: {}",
                request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);

        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "An unexpected error occurred. Please try again later.", request);
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String code,
                                                         String message, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.of(
                status.value(),
                status.getReasonPhrase(),
                code,
                message,
                request.getRequestURI(),
                getCorrelationId()
        );
        return ResponseEntity.status(status).body(error);
    }

    private String getCorrelationId() {
        return MDC.get("correlationId");
    }
}
