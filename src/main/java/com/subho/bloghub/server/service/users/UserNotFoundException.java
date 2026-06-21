package com.subho.bloghub.server.service.users;

import com.subho.bloghub.server.exception.ResourceNotFoundException;

/**
 * Thrown when a handle/id doesn't resolve to a user. Extends
 * {@link ResourceNotFoundException} so {@code GlobalExceptionHandler}
 * translates it into a standard 404 {@code ErrorResponseDTO} automatically.
 */
public class UserNotFoundException extends ResourceNotFoundException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
