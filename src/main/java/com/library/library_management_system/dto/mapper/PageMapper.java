package com.library.library_management_system.dto.mapper;

import com.library.library_management_system.dto.response.PagedResponse;
import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

@UtilityClass
public class PageMapper {

    public static <T, R> PagedResponse<R> toPagedResponse(Page<T> page, Function<T, R> mapper) {
        List<R> content = page.getContent().stream()
                .map(mapper)
                .toList();

        return PagedResponse.<R>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    public static <T> PagedResponse<T> toPagedResponse(Page<T> page) {
        return toPagedResponse(page, Function.identity());
    }

    public static <T> PagedResponse<T> toPagedResponse(List<T> content, int page, int size, long totalElements) {
        return PagedResponse.of(content, page, size, totalElements);
    }
}