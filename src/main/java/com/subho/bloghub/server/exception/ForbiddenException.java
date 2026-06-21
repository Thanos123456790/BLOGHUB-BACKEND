package com.subho.bloghub.server.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when the caller is identified but not permitted to perform the
 * requested action (e.g. editing a blog/comment they don't own). Translated
 * to a 403 by {@link GlobalExceptionHandler}.
 *
 * NOTE: real enforcement of this depends on resolving the caller's identity
 * from the access token, which is explicitly out of scope right now (auth is
 * not implemented). The exception type and handler are wired up so that
 * ownership checks can be turned on with a one-line change once auth lands.
 */
public class ForbiddenException extends ApplicationException {

    public ForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, "FORBIDDEN", message);
    }
}
