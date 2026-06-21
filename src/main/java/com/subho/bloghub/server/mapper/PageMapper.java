package com.subho.bloghub.server.mapper;

import org.springframework.data.domain.Page;

import java.util.function.Function;

/**
 * Small helper to keep "map a Page of entities to a Page of response DTOs"
 * uniform across every service, using each module's {@link GenericMapper}.
 * Spring Data's {@code Page.map} already preserves paging metadata
 * (page number, size, total elements/pages) without re-querying the DB —
 * this just removes the boilerplate method reference at every call site.
 */
public final class PageMapper {

    private PageMapper() {
    }

    public static <E, RS> Page<RS> toResponsePage(Page<E> page, GenericMapper<E, ?, RS> mapper) {
        return page.map(mapper::toResponse);
    }

    public static <E, RS> Page<RS> toResponsePage(Page<E> page, Function<E, RS> mappingFn) {
        return page.map(mappingFn);
    }
}
