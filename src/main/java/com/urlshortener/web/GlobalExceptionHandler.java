package com.urlshortener.web;

import com.urlshortener.web.dto.ApiError;
import com.urlshortener.web.error.AliasConflictException;
import com.urlshortener.web.error.AliasReservedException;
import com.urlshortener.web.error.InvalidUrlException;
import com.urlshortener.web.error.LinkNotFoundException;
import com.urlshortener.web.error.ShortCodeGenerationException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvalidUrlException.class)
    public ResponseEntity<ApiError> handleInvalidUrl(InvalidUrlException ex) {
        return ResponseEntity.badRequest()
                .body(ApiError.of(400, "invalid_url", ex.getMessage()));
    }

    @ExceptionHandler(AliasConflictException.class)
    public ResponseEntity<ApiError> handleAliasConflict(AliasConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError.of(409, "alias_conflict", ex.getMessage()));
    }

    @ExceptionHandler(AliasReservedException.class)
    public ResponseEntity<ApiError> handleAliasReserved(AliasReservedException ex) {
        return ResponseEntity.badRequest()
                .body(ApiError.of(400, "alias_reserved", ex.getMessage()));
    }

    @ExceptionHandler(LinkNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(LinkNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiError.of(404, "not_found", ex.getMessage()));
    }

    @ExceptionHandler(ShortCodeGenerationException.class)
    public ResponseEntity<ApiError> handleGenFailure(ShortCodeGenerationException ex) {
        log.warn("Short-code generation exhausted retries: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiError.of(503, "short_code_generation_failed", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();
        return ResponseEntity.badRequest()
                .body(ApiError.of(400, "validation_failed", "Request validation failed", details));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest()
                .body(ApiError.of(400, "malformed_request", "Request body is missing or malformed"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegal(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(ApiError.of(400, "bad_request", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.internalServerError()
                .body(ApiError.of(500, "internal_error", "Unexpected error"));
    }
}
