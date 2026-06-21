package com.subho.bloghub.server.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown for file-upload problems: disallowed content type, file too large,
 * empty file, or an underlying S3 failure. Translated to a 400/502 by
 * {@link GlobalExceptionHandler} depending on whether it's a client input
 * problem or an upstream storage failure.
 */
public class FileStorageException extends ApplicationException {

    public FileStorageException(String message) {
        super(HttpStatus.BAD_REQUEST, "FILE_STORAGE_ERROR", message);
    }

    public FileStorageException(HttpStatus status, String message) {
        super(status, "FILE_STORAGE_ERROR", message);
    }
}
