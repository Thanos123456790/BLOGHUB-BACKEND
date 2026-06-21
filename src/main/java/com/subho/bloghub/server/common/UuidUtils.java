package com.subho.bloghub.server.common;

import com.subho.bloghub.server.exception.BadRequestException;

import java.util.UUID;

/**
 * Centralises UUID parsing for path variables. Without this, a malformed id
 * in the URL (e.g. {@code /api/v1/blogs/not-a-uuid}) throws
 * {@link IllegalArgumentException} deep in a service method, which the
 * global handler would otherwise report as a 500. Routing it through here
 * turns it into a clean, expected 400 instead.
 */
public final class UuidUtils {

    private UuidUtils() {
    }

    public static UUID parse(String value, String fieldName) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BadRequestException("Invalid " + fieldName + ": must be a valid UUID");
        }
    }
}
