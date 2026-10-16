package com.mosaic.common.dto;

import java.util.List;

/**
 * Generic paginated response wrapper.
 *
 * All list endpoints return data wrapped in this structure so
 * clients always know the pagination state.
 *
 * @param <T> the type of elements in the page
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {

    /**
     * Creates a PageResponse from Spring's Page object fields.
     */
    public static <T> PageResponse<T> of(List<T> content, int page, int size,
                                         long totalElements, int totalPages) {
        return new PageResponse<>(
                content, page, size, totalElements, totalPages,
                page == 0,
                page >= totalPages - 1
        );
    }
}
