package com.subho.bloghub.server.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown for semantically invalid requests that pass bean validation but are
 * still wrong (e.g. an unknown reaction type slipping past the enum, a
 * malformed pagination request, a forbidden state transition that isn't a
 * 403/409). Translated to a 400 by {@link GlobalExceptionHandler}.
 */
public class BadRequestException extends ApplicationException {

    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, "BAD_REQUEST", message);
    }
}
