package com.valdifly.infrastructure.web;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Standard payload for list endpoints, wrapped in {@link ApiResponse}:
 *
 * <pre>{@code
 * { "code": 0, "message": "success", "data": {
 *     "items": [ ... ],
 *     "pagination": { "page": 0, "size": 20, "total": 137, "totalPages": 7, "hasNext": true }
 * } } }
 * }</pre>
 *
 * Two pagination modes, distinguished by which fields are set (unset fields are
 * omitted from the wire, hence NON_NULL and boxed types):
 * <ul>
 *   <li>offset mode — {@link #of(List, int, int, long)}: page/size/total/totalPages/hasNext;</li>
 *   <li>cursor mode — {@link #ofCursor(List, int, boolean, String)}: size/hasNext/nextCursor,
 *       for collections where a total count is unknown or too expensive.</li>
 * </ul>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PaginatedData<T>(List<T> items, Pagination pagination) {

    /** Offset mode; {@code page} is 0-based. */
    public static <T> PaginatedData<T> of(List<T> items, int page, int size, long total) {
        return new PaginatedData<>(items, Pagination.ofPage(page, size, total));
    }

    /** Cursor mode; {@code nextCursor} is null when there is no further page. */
    public static <T> PaginatedData<T> ofCursor(List<T> items, int size, boolean hasNext, String nextCursor) {
        return new PaginatedData<>(items, Pagination.ofCursor(size, hasNext, nextCursor));
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Pagination(
            Integer page,
            Integer size,
            Long total,
            Integer totalPages,
            Boolean hasNext,
            String nextCursor
    ) {

        /** Offset mode; {@code page} is 0-based. */
        public static Pagination ofPage(int page, int size, long total) {
            int totalPages = size > 0 ? (int) Math.ceil((double) total / size) : 0;
            return new Pagination(page, size, total, totalPages, page + 1 < totalPages, null);
        }

        /** Cursor mode; page/total/totalPages are omitted from the wire. */
        public static Pagination ofCursor(int size, boolean hasNext, String nextCursor) {
            return new Pagination(null, size, null, null, hasNext, nextCursor);
        }
    }
}
