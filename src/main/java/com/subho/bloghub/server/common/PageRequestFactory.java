package com.subho.bloghub.server.common;

import com.subho.bloghub.server.config.PaginationProperties;
import com.subho.bloghub.server.exception.BadRequestException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Single place that turns raw page/size query params into a bounded
 * {@link Pageable}. Every paginated endpoint should go through this rather
 * than calling {@code PageRequest.of(page, size)} directly, so:
 *  - page size is always capped (protects the DB/heap from a caller asking
 *    for size=100000 — an easy denial-of-service vector otherwise)
 *  - negative page/size values are rejected instead of silently coerced
 *  - default sizing/sorting stays consistent across every module
 */
public class PageRequestFactory {

    private final PaginationProperties properties;

    public PageRequestFactory(PaginationProperties properties) {
        this.properties = properties;
    }

    public Pageable of(int page, int size) {
        return of(page, size, null);
    }

    public Pageable of(int page, int size, Sort sort) {
        if (page < 0) {
            throw new BadRequestException("Page index must not be negative");
        }
        if (size <= 0) {
            throw new BadRequestException("Page size must be greater than zero");
        }

        int boundedSize = Math.min(size, properties.getMaxPageSize());

        return sort == null
                ? PageRequest.of(page, boundedSize)
                : PageRequest.of(page, boundedSize, sort);
    }

    public int defaultPageSize() {
        return properties.getDefaultPageSize();
    }
}
