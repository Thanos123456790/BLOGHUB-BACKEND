package com.subho.bloghub.server.exception;

import org.springframework.http.HttpStatus;

/**
 * Base type for all handled application exceptions. Carries the HTTP status
 * and a short machine-readable error code so {@link GlobalExceptionHandler}
 * can translate any subclass into a consistent {@code ErrorResponseDTO}
 * without each exception needing to know about the web layer.
 */
public class ApplicationException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    public ApplicationException(HttpStatus status, String errorCode, String message) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
