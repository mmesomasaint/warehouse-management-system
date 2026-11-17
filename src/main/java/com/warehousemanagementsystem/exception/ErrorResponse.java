package com.warehousemanagementsystem.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.OffsetDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        Map<String, String> validationErrors
) {
    // Constructor for general errors
    public ErrorResponse(int status, String error, String message) {
        this(OffsetDateTime.now(), status, error, message, null);
    }

    // Constructor for validation field errors
    public ErrorResponse(int status, String error, String message, Map<String, String> validationErrors) {
        this(OffsetDateTime.now(), status, error, message, validationErrors);
    }
}