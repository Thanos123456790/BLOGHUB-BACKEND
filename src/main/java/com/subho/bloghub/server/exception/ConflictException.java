package com.subho.bloghub.server.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown for state conflicts — duplicate follow, duplicate bookmark, unique
 * constraint violations, self-follow, etc. Translated to a 409 by
 * {@link GlobalExceptionHandler}.
 */
public class ConflictException extends ApplicationException {

    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, "CONFLICT", message);
    }
}
