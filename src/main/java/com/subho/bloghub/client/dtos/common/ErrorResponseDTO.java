package com.subho.bloghub.client.dtos.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Standard error response returned on API failures")
public class ErrorResponseDTO {

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "Short error code", example = "VALIDATION_FAILED")
    private String error;

    @Schema(description = "Human-readable error message", example = "Request validation failed")
    private String message;

    @Schema(description = "Request path that caused the error", example = "/api/blogs")
    private String path;

    @Schema(description = "Timestamp of the error")
    private Instant timestamp;

    @Schema(description = "Field-level validation errors (populated on 400 responses)")
    private List<FieldError> fieldErrors;

    // ── Nested field error ─────────────────────────────────────────────────

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "A single field-level validation error")
    public static class FieldError {

        @Schema(description = "The field that failed validation", example = "email")
        private String field;

        @Schema(description = "The rejected value", example = "not-an-email")
        private Object rejectedValue;

        @Schema(description = "Validation failure reason", example = "Must be a valid email address")
        private String message;
    }
}

