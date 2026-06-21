package com.subho.bloghub.server.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a requested resource (blog, comment, user, tag, etc.) does not
 * exist. Translated to a 404 by {@link GlobalExceptionHandler}.
 */
public class ResourceNotFoundException extends ApplicationException {

    public ResourceNotFoundException(String resource, Object identifier) {
        super(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND",
                resource + " not found: " + identifier);
    }

    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", message);
    }
}
